"""Brzi sanity check novog modela na test upitima."""
import sys, io, json, torch, torch.nn as nn, joblib, pandas as pd
from sentence_transformers import SentenceTransformer

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')


class ClassifierHead(nn.Module):
    def __init__(self, in_dim, num_classes):
        super().__init__()
        self.net = nn.Sequential(
            nn.Linear(in_dim, 384), nn.GELU(), nn.Dropout(0.2),
            nn.Linear(384, num_classes),
        )

    def forward(self, x): return self.net(x)


enc_name = open('encoder_model_name.txt').read().strip()
print(f'Loading {enc_name}...')
encoder = SentenceTransformer(enc_name, device='cpu')
le = joblib.load('label_encoder.pkl')
ckpt = torch.load('classifier_head.pth', map_location='cpu', weights_only=True)
head = ClassifierHead(ckpt['in_dim'], ckpt['num_classes'])
head.load_state_dict(ckpt['state_dict'])
head.eval()

with open('category_names.json', encoding='utf-8') as f:
    names = {int(k): v for k, v in json.load(f).items()}


def predict(title, k=5):
    with torch.no_grad():
        emb = encoder.encode([title], convert_to_tensor=True, normalize_embeddings=True, show_progress_bar=False)
        probs = torch.softmax(head(emb), dim=1)[0]
        top = torch.topk(probs, k)
    print(f"\n> {title!r}")
    for i in range(k):
        idx = top.indices[i].item()
        cid = int(le.inverse_transform([idx])[0])
        print(f"  {top.values[i].item()*100:5.2f}%  {names.get(cid, '?')} ({cid})")


tests = [
    # Iz prosle konverzacije - stari model je davao smesne rezultate
    'samsung', 'iphone', 'karcher', 'perac pod pritiskom',
    # Realni oglasi (nisu u trening skupu - iz oglasi_podaci)
    'Iznajmljivanje ventilatora na vodu',
    'Sup daske',
    'Iznajmi haljinu',
    # Sa dajnadan (nisu u train)
    'stan na dan centar grada',
    'Sony PS5/PS4 Pro',
    'Akumulatorska busilica/cekic Makita DHR 241',
    # Testovi na tesko: brend + kolokvijalno
    'Makita busilica akumulatorska',
    'DJI Mavic 3 pro',
    'Roll up baner za sajam',
    # Deklinacije i tipografske
    'iznajmljujem sator za svadbu 100 gostiju',
    'kompresor za vazduh 200l',
]

for t in tests:
    predict(t, k=3)
