import os
import time
from collections import deque
from typing import Deque, Dict, Optional

from fastapi import FastAPI, Header, HTTPException, Request
from fastapi.responses import StreamingResponse
from pydantic import BaseModel

from chatbot import agent, stream_answer

app = FastAPI(title="RentRentOut AI Service")

# ─── Auth (shared secret between Spring Boot backend and ML service) ─────────
# If AI_SERVICE_API_KEY is unset (local dev), auth is skipped with a warning.
_API_KEY = os.getenv("AI_SERVICE_API_KEY", "").strip()
if not _API_KEY:
    print("WARNING: AI_SERVICE_API_KEY not set — /api/chat endpoints are unauthenticated.")


def _require_api_key(x_internal_api_key: Optional[str]) -> None:
    if not _API_KEY:
        return
    if not x_internal_api_key or x_internal_api_key != _API_KEY:
        raise HTTPException(status_code=401, detail="Invalid or missing X-Internal-API-Key.")


# ─── Rate limit (sliding window, per user, in-process) ────────────────────────
# 20 requests per 60 seconds per userId. Good enough for a single ML replica;
# if we scale horizontally, swap for Redis.
_RATE_LIMIT_MAX = 20
_RATE_LIMIT_WINDOW_SEC = 60
_rate_buckets: Dict[str, Deque[float]] = {}


def _check_rate_limit(user_id: str) -> None:
    now = time.monotonic()
    bucket = _rate_buckets.setdefault(user_id, deque())
    while bucket and (now - bucket[0]) > _RATE_LIMIT_WINDOW_SEC:
        bucket.popleft()
    if len(bucket) >= _RATE_LIMIT_MAX:
        raise HTTPException(status_code=429, detail="Previše zahteva. Sačekajte minut.")
    bucket.append(now)


# ─── Category prediction: multilingual encoder + trained MLP head ────────────
# Pipeline: title -> sentence-transformer (frozen) -> 768d embedding
#                -> MLP head -> softmax over 644 leaf categories
_encoder = None
_head = None
_label_encoder = None
_CONFIDENCE_THRESHOLD = float(os.getenv("CATEGORY_CONFIDENCE_THRESHOLD", "0.15"))
_TOP_K = int(os.getenv("CATEGORY_TOP_K", "5"))

try:
    import torch
    import torch.nn as nn
    import joblib
    from sentence_transformers import SentenceTransformer

    class ClassifierHead(nn.Module):
        def __init__(self, in_dim, num_classes):
            super().__init__()
            self.net = nn.Sequential(
                nn.Linear(in_dim, 384),
                nn.GELU(),
                nn.Dropout(0.2),
                nn.Linear(384, num_classes),
            )

        def forward(self, x):
            return self.net(x)

    with open("encoder_model_name.txt", "r", encoding="utf-8") as f:
        encoder_name = f.read().strip()

    print(f"Loading sentence-transformer encoder: {encoder_name}")
    _encoder = SentenceTransformer(encoder_name, device="cpu")

    print("Loading classifier head and label encoder...")
    _label_encoder = joblib.load("label_encoder.pkl")
    ckpt = torch.load("classifier_head.pth", map_location="cpu", weights_only=True)
    _head = ClassifierHead(ckpt["in_dim"], ckpt["num_classes"])
    _head.load_state_dict(ckpt["state_dict"])
    _head.eval()

    # warmup na prvom pozivu je spor - odmah radimo dry run
    with torch.no_grad():
        warm = _encoder.encode(
            ["warmup"], convert_to_tensor=True, normalize_embeddings=True,
            show_progress_bar=False,
        )
        _head(warm)

    print(f"Category model ready. Classes: {len(_label_encoder.classes_)}")
except Exception as e:
    print(f"Category model not loaded (chatbot-only mode): {e}")


# ─── Health ───────────────────────────────────────────────────────────────────
@app.get("/health")
def health():
    return {
        "status": "ok",
        "category_model": _encoder is not None and _head is not None,
        "chatbot": True,
        "auth_enabled": bool(_API_KEY),
    }


# ─── Category prediction endpoint ─────────────────────────────────────────────
class AdRequest(BaseModel):
    title: str


@app.post("/api/predict-category")
def predict_category(request: AdRequest):
    if _encoder is None or _head is None:
        return {"error": "Category model not available."}

    import torch
    title = (request.title or "").strip()
    if not title:
        return {
            "title": request.title,
            "predicted_category_ids": [],
            "suggestions": [],
            "all_suggestions": [],
            "threshold": _CONFIDENCE_THRESHOLD,
        }

    with torch.no_grad():
        emb = _encoder.encode(
            [title],
            convert_to_tensor=True,
            normalize_embeddings=True,
            show_progress_bar=False,
        )
        logits = _head(emb)
        probs = torch.softmax(logits, dim=1)[0]
        k = min(_TOP_K, probs.numel())
        top = torch.topk(probs, k=k)
        top_indices = top.indices.tolist()
        top_scores = top.values.tolist()

    suggestions = []
    for idx, score in zip(top_indices, top_scores):
        cat_id = int(_label_encoder.inverse_transform([idx])[0])
        suggestions.append({
            "category_id": cat_id,
            "confidence": round(float(score), 4),
        })

    filtered = [s for s in suggestions if s["confidence"] >= _CONFIDENCE_THRESHOLD]

    return {
        "title": request.title,
        "predicted_category_ids": [s["category_id"] for s in filtered],
        "suggestions": filtered,
        "all_suggestions": suggestions,
        "threshold": _CONFIDENCE_THRESHOLD,
    }


# ─── Chatbot ──────────────────────────────────────────────────────────────────
class ChatRequest(BaseModel):
    message: str
    userId: str
    userContext: str = ""


@app.post("/api/chat")
async def chat(
    request: ChatRequest,
    x_internal_api_key: Optional[str] = Header(default=None, alias="X-Internal-API-Key"),
):
    _require_api_key(x_internal_api_key)
    _check_rate_limit(request.userId)

    config = {"configurable": {"thread_id": request.userId}}
    result = await agent.ainvoke(
        {
            "question": request.message,
            "user_context": request.userContext,
            "thread_id": request.userId,
        },
        config=config,
    )
    return {"reply": result["answer"]}


@app.post("/api/chat/stream")
async def chat_stream(
    request: ChatRequest,
    req: Request,
    x_internal_api_key: Optional[str] = Header(default=None, alias="X-Internal-API-Key"),
):
    _require_api_key(x_internal_api_key)
    _check_rate_limit(request.userId)

    async def event_gen():
        try:
            async for token in stream_answer(request.message, request.userId, request.userContext):
                if await req.is_disconnected():
                    break
                # SSE frames: escape newlines because SSE data lines are line-based.
                safe = token.replace("\r", "").replace("\n", "\\n")
                yield f"data: {safe}\n\n"
            yield "event: done\ndata: [DONE]\n\n"
        except Exception as e:
            yield f"event: error\ndata: {str(e)}\n\n"

    return StreamingResponse(
        event_gen(),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "Connection": "keep-alive",
            "X-Accel-Buffering": "no",
        },
    )
