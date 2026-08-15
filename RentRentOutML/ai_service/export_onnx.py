"""
Exports encoder + classifier head to ONNX.
Runs in Docker builder stage. For local dev: python export_onnx.py
Needs: torch, sentence-transformers, onnxruntime, joblib
"""
import os
import torch
import torch.nn as nn


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


class EncoderWrapper(nn.Module):
    def __init__(self, model):
        super().__init__()
        self.model = model

    def forward(self, input_ids, attention_mask):
        return self.model(input_ids=input_ids, attention_mask=attention_mask).last_hidden_state


# ── Classifier head ────────────────────────────────────────────────────────────
print("Exporting classifier head...")
ckpt = torch.load("classifier_head.pth", map_location="cpu", weights_only=True)
head = ClassifierHead(ckpt["in_dim"], ckpt["num_classes"])
head.load_state_dict(ckpt["state_dict"])
head.eval()

dummy = torch.zeros(1, ckpt["in_dim"])
with torch.no_grad():
    torch.onnx.export(
        head, dummy, "classifier_head.onnx",
        input_names=["embedding"],
        output_names=["logits"],
        dynamic_axes={"embedding": {0: "batch"}, "logits": {0: "batch"}},
        opset_version=14,
    )
# Inline external data into a single self-contained file (torch may split it).
import onnx as _onnx
_onnx.save_model(_onnx.load("classifier_head.onnx"), "classifier_head.onnx", save_as_external_data=False)
if os.path.exists("classifier_head.onnx.data"):
    os.remove("classifier_head.onnx.data")
print(f"  classifier_head.onnx: {os.path.getsize('classifier_head.onnx') / 1e3:.0f}KB")

# ── Encoder ───────────────────────────────────────────────────────────────────
from sentence_transformers import SentenceTransformer

MODEL = "sentence-transformers/paraphrase-multilingual-mpnet-base-v2"
print(f"Loading {MODEL}...")
st = SentenceTransformer(MODEL, device="cpu")
tokenizer = st[0].tokenizer
auto_model = st[0].auto_model

print("Saving tokenizer...")
os.makedirs("tokenizer", exist_ok=True)
tokenizer.save_pretrained("tokenizer/")

wrapper = EncoderWrapper(auto_model)
wrapper.eval()

sample = tokenizer(
    "test", return_tensors="pt", padding="max_length", max_length=128, truncation=True
)

print("Exporting encoder (FP32)...")
with torch.no_grad():
    torch.onnx.export(
        wrapper,
        (sample["input_ids"], sample["attention_mask"]),
        "encoder_fp32.onnx",
        input_names=["input_ids", "attention_mask"],
        output_names=["last_hidden_state"],
        dynamic_axes={
            "input_ids": {0: "batch", 1: "seq"},
            "attention_mask": {0: "batch", 1: "seq"},
            "last_hidden_state": {0: "batch", 1: "seq"},
        },
        opset_version=14,
    )

print("Quantizing encoder (INT8)...")
from onnxruntime.quantization import quantize_dynamic, QuantType
quantize_dynamic("encoder_fp32.onnx", "encoder.onnx", weight_type=QuantType.QInt8)
os.remove("encoder_fp32.onnx")
print(f"  encoder.onnx: {os.path.getsize('encoder.onnx') / 1e6:.0f}MB")
print("Done.")
