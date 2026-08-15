# ─── Stage 1: Export ONNX models ──────────────────────────────────────────────
# Torch + sentence-transformers su samo ovde — ne idu u produkcioni image.
FROM python:3.11-slim AS exporter

WORKDIR /app

RUN pip install --no-cache-dir torch --index-url https://download.pytorch.org/whl/cpu && \
    pip install --no-cache-dir sentence-transformers>=3.0.0 onnxruntime joblib onnxscript onnx

COPY classifier_head.pth label_encoder.pkl export_onnx.py ./
RUN python export_onnx.py

# ─── Stage 2: Runtime (~600MB umesto ~1.5GB) ──────────────────────────────────
FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

ENV HF_HOME=/app/.hf_cache

COPY . .
COPY --from=exporter /app/encoder.onnx ./encoder.onnx
COPY --from=exporter /app/classifier_head.onnx ./classifier_head.onnx
COPY --from=exporter /app/tokenizer ./tokenizer

RUN mkdir -p /app/data /app/.hf_cache

EXPOSE 8000

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
