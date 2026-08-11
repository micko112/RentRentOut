FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .

RUN pip install --no-cache-dir -r requirements.txt && \
    pip install --no-cache-dir torch --index-url https://download.pytorch.org/whl/cpu

# Pre-download sentence-transformer model tokom build-a (~470MB) da runtime nema cold-start download.
# HF cache se drzi u /app/.hf_cache.
COPY encoder_model_name.txt .
ENV HF_HOME=/app/.hf_cache
RUN python -c "from sentence_transformers import SentenceTransformer; \
    name=open('encoder_model_name.txt').read().strip(); \
    SentenceTransformer(name)"

COPY . .

RUN mkdir -p /app/data

EXPOSE 8000

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
