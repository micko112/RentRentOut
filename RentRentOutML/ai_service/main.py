from fastapi import FastAPI
from pydantic import BaseModel
from chatbot import agent

app = FastAPI(title="RentRentOut AI Service")

# ─── Category prediction model (torch + scikit-learn) ────────────────────────
# Loaded only if artifacts are available (production/Docker).
# Locally for chatbot dev this block is skipped if packages are not installed.
_category_model = None
_vectorizer = None
_label_encoder = None

try:
    import torch
    import torch.nn as nn
    import joblib

    class RentRentOutCategorizer(nn.Module):
        def __init__(self, input_size, num_classes):
            super().__init__()
            self.network = nn.Sequential(
                nn.Linear(input_size, 512),
                nn.ReLU(),
                nn.Dropout(0.3),
                nn.Linear(512, 256),
                nn.ReLU(),
                nn.Dropout(0.2),
                nn.Linear(256, 128),
                nn.ReLU(),
                nn.Dropout(0.1),
                nn.Linear(128, num_classes),
            )

        def forward(self, x):
            return self.network(x)

    print("Loading category model and artifacts...")
    _vectorizer = joblib.load("tfidf_vectorizer.pkl")
    _label_encoder = joblib.load("label_encoder.pkl")
    input_size = len(_vectorizer.vocabulary_)
    num_classes = len(_label_encoder.classes_)
    _category_model = RentRentOutCategorizer(input_size, num_classes)
    state_dict = torch.load("rentrentout_model.pth", map_location=torch.device("cpu"), weights_only=True)
    # remap legacy Serbian attribute name to English
    state_dict = {k.replace("mreza.", "network."): v for k, v in state_dict.items()}
    _category_model.load_state_dict(state_dict)
    _category_model.eval()
    print("Category model loaded successfully.")
except Exception as e:
    print(f"Category model not loaded (chatbot-only mode): {e}")


# ─── Category prediction endpoint ────────────────────────────────────────────

class AdRequest(BaseModel):
    title: str


@app.post("/api/predict-category")
def predict_category(request: AdRequest):
    if _category_model is None:
        return {"error": "Category model not available."}

    import torch
    X_new = _vectorizer.transform([request.title]).toarray()
    X_tensor = torch.tensor(X_new, dtype=torch.float32)
    with torch.no_grad():
        output = _category_model(X_tensor)
        top5_indices = output.topk(5, dim=1).indices[0].tolist()
    top5_ids = [int(_label_encoder.inverse_transform([i])[0]) for i in top5_indices]
    return {"title": request.title, "predicted_category_ids": top5_ids}


# ─── Chatbot endpoint ─────────────────────────────────────────────────────────

class ChatRequest(BaseModel):
    message: str
    userId: str
    userContext: str = ""


@app.post("/api/chat")
def chat(request: ChatRequest):
    config = {"configurable": {"thread_id": request.userId}}
    result = agent.invoke(
        {"question": request.message, "user_context": request.userContext},
        config=config,
    )
    return {"reply": result["answer"]}
