import os
import time
from collections import deque
from typing import Deque, Dict, Optional

from fastapi import FastAPI, Header, HTTPException, Request
from fastapi.responses import StreamingResponse
from pydantic import BaseModel

from chatbot import agent, stream_answer
from chatbot_logger import log_event

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


# ─── Category prediction: ONNX encoder + ONNX classifier head ────────────────
# Pipeline: title -> tokenizer -> encoder.onnx (768d) -> classifier_head.onnx -> softmax
_tokenizer = None
_encoder_session = None
_head_session = None
_label_encoder = None
_CONFIDENCE_THRESHOLD = float(os.getenv("CATEGORY_CONFIDENCE_THRESHOLD", "0.03"))
_TOP_K = int(os.getenv("CATEGORY_TOP_K", "5"))

try:
    import numpy as np
    import joblib
    import onnxruntime as ort
    from transformers import AutoTokenizer

    _tokenizer = AutoTokenizer.from_pretrained("tokenizer/")
    _encoder_session = ort.InferenceSession("encoder.onnx", providers=["CPUExecutionProvider"])
    _label_encoder = joblib.load("label_encoder.pkl")
    _head_session = ort.InferenceSession("classifier_head.onnx", providers=["CPUExecutionProvider"])

    # warmup
    _wi = _tokenizer(["warmup"], return_tensors="np", padding=True, truncation=True, max_length=128)
    _wf = {"input_ids": _wi["input_ids"].astype(np.int64), "attention_mask": _wi["attention_mask"].astype(np.int64)}
    _wh = _encoder_session.run(["last_hidden_state"], _wf)[0]
    _wm = _wi["attention_mask"][:, :, np.newaxis].astype(np.float32)
    _we = ((_wh * _wm).sum(1) / _wm.sum(1)).astype(np.float32)
    _head_session.run(["logits"], {"embedding": _we})

    print(f"Category model ready (ONNX). Classes: {len(_label_encoder.classes_)}")
except Exception as e:
    print(f"Category model not loaded (chatbot-only mode): {e}")


# ─── Health ───────────────────────────────────────────────────────────────────
@app.get("/health")
def health():
    return {
        "status": "ok",
        "category_model": _encoder_session is not None and _head_session is not None,
        "chatbot": True,
        "auth_enabled": bool(_API_KEY),
    }


# ─── Category prediction endpoint ─────────────────────────────────────────────
class AdRequest(BaseModel):
    title: str


@app.post("/api/predict-category")
def predict_category(request: AdRequest):
    if _encoder_session is None or _head_session is None:
        return {"error": "Category model not available."}

    import numpy as np

    title = (request.title or "").strip()
    if not title:
        return {
            "title": request.title,
            "predicted_category_ids": [],
            "suggestions": [],
            "all_suggestions": [],
            "threshold": _CONFIDENCE_THRESHOLD,
        }

    # Tokenize + encode
    inputs = _tokenizer([title], return_tensors="np", padding=True, truncation=True, max_length=128)
    feed = {
        "input_ids": inputs["input_ids"].astype(np.int64),
        "attention_mask": inputs["attention_mask"].astype(np.int64),
    }
    hidden = _encoder_session.run(["last_hidden_state"], feed)[0]  # [1, seq, 768]

    # Mean pooling + L2 normalize
    mask = inputs["attention_mask"][:, :, np.newaxis].astype(np.float32)
    emb = (hidden * mask).sum(1) / mask.sum(1)
    emb = (emb / (np.linalg.norm(emb, axis=1, keepdims=True) + 1e-8)).astype(np.float32)

    # Classify
    logits = _head_session.run(["logits"], {"embedding": emb})[0][0]
    logits -= logits.max()
    probs = np.exp(logits)
    probs /= probs.sum()

    k = min(_TOP_K, len(probs))
    top_idx = np.argpartition(probs, -k)[-k:]
    top_idx = top_idx[np.argsort(probs[top_idx])[::-1]]

    suggestions = [
        {
            "category_id": int(_label_encoder.inverse_transform([i])[0]),
            "confidence": round(float(probs[i]), 4),
        }
        for i in top_idx
    ]
    filtered = [s for s in suggestions if s["confidence"] >= _CONFIDENCE_THRESHOLD]

    log_event("system", "predict_category", {
        "title": request.title,
        "top5": suggestions[:5],
    })

    return {
        "title": request.title,
        "predicted_category_ids": [s["category_id"] for s in suggestions],
        "suggestions": suggestions,
        "all_suggestions": suggestions,
        "filtered_suggestions": filtered,
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
            import traceback
            print(f"[CHATBOT ERROR] user={request.userId} msg={request.message!r}\n{traceback.format_exc()}", flush=True)
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
