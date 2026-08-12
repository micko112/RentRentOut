FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .

RUN pip install --no-cache-dir -r requirements.txt && \
    pip install --no-cache-dir torch --index-url https://download.pytorch.org/whl/cpu

# HF model se skida pri prvom pokretanju i kešira u mountovanom volumenu (/app/.hf_cache).
# Ne downloadujemo tokom build-a da bi CI image ostao mali.
ENV HF_HOME=/app/.hf_cache

COPY . .

RUN mkdir -p /app/data

EXPOSE 8000

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
