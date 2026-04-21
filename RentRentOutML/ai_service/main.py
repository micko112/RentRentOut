from fastapi import FastAPI
from pydantic import BaseModel
from chatbot import agent

app = FastAPI(title="RentRentOut AI Service")

# ─── Category prediction model (torch + scikit-learn) ────────────────────────
# Učitava se samo ako su artifakti dostupni (produkcija/Docker).
# Lokalno za dev chatbota ovaj blok se preskače ako paketi nisu instalirani.
_category_model = None
_vectorizer = None
_label_encoder = None

try:
    import torch
    import torch.nn as nn
    import joblib

    class RentRentOutKategorizator(nn.Module):
        def __init__(self, ulazna_velicina, broj_klasa):
            super().__init__()
            self.network = nn.Sequential(
                nn.Linear(ulazna_velicina, 512),
                nn.ReLU(),
                nn.Dropout(0.3),
                nn.Linear(512, 256),
                nn.ReLU(),
                nn.Dropout(0.2),
                nn.Linear(256, 128),
                nn.ReLU(),
                nn.Dropout(0.1),
                nn.Linear(128, broj_klasa),
            )

        def forward(self, x):
            return self.network(x)

    print("Učitavam category model i artifakte...")
    _vectorizer = joblib.load("tfidf_vectorizer.pkl")
    _label_encoder = joblib.load("label_encoder.pkl")
    ulazna_velicina = len(_vectorizer.vocabulary_)
    broj_klasa = len(_label_encoder.classes_)
    _category_model = RentRentOutKategorizator(ulazna_velicina, broj_klasa)
    _category_model.load_state_dict(
        torch.load("rentrentout_model.pth", map_location=torch.device("cpu"), weights_only=True)
    )
    _category_model.eval()
    print("Category model uspešno učitan.")
except Exception as e:
    print(f"Category model nije učitan (samo chatbot mod): {e}")


# ─── Category prediction endpoint ────────────────────────────────────────────

class AdRequest(BaseModel):
    title: str


@app.post("/api/predict-category")
def predict_category(request: AdRequest):
    if _category_model is None:
        return {"error": "Category model nije dostupan."}

    import torch
    X_novi = _vectorizer.transform([request.title]).toarray()
    X_tenzor = torch.tensor(X_novi, dtype=torch.float32)
    with torch.no_grad():
        izlaz = _category_model(X_tenzor)
        top5_indeksi = izlaz.topk(5, dim=1).indices[0].tolist()
    top5_ids = [int(_label_encoder.inverse_transform([i])[0]) for i in top5_indeksi]
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
