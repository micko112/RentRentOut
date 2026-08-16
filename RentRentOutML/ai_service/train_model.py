"""
Fine-tune category classifier na embeddings iz multilingual sentence-transformer-a.

Arhitektura:
  Text -> paraphrase-multilingual-mpnet-base-v2 (frozen, 768d)
       -> MLP head [768 -> 384 -> num_classes]
       -> softmax

Radi na CPU. Total training time: ~5-10 min posle download-a modela.

Izlazi (svi u istom folderu):
  - encoder_model_name.txt       (ime HF modela koji sluzi kao encoder)
  - classifier_head.pth          (MLP head weights)
  - label_encoder.pkl            (mapiranje idx <-> category_id)
  - training_report.txt          (metrike)
"""
import sys, io, os, random, json
import numpy as np
import pandas as pd
import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import DataLoader, TensorDataset
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder
import joblib

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

SEED = 42
random.seed(SEED); np.random.seed(SEED); torch.manual_seed(SEED)

ENCODER_NAME = 'sentence-transformers/paraphrase-multilingual-mpnet-base-v2'
EMB_DIM = 768
BATCH_ENCODE = 64
EPOCHS = 40
LR = 1e-3
BATCH_TRAIN = 64
WEIGHT_DECAY = 1e-4
PATIENCE = 6

device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
print(f'Device: {device}')

# ---------- 1) Load data ----------
df = pd.read_csv('training_data.csv', encoding='utf-8-sig')
n_real = (df['source'] == 'real').sum()
print(f'Loaded {len(df)} training rows')
print(f'  real: {n_real}, '
      f'hygglo: {(df["source"]=="hygglo").sum()}, '
      f'synthetic_sr: {(df["source"]=="synthetic_sr").sum()}')

le = LabelEncoder()
df['label'] = le.fit_transform(df['category_id'])
num_classes = len(le.classes_)
print(f'Num classes: {num_classes}')

# ---------- 2) Compute (or load cached) embeddings — samo unikalni redovi ----------
# Cache je baziran na ORIGINALNOM df (bez oversampling-a) da enkodovanje traje
# ~35 min umesto ~60 min. Oversample se radi posle, na numpy nivouu.
CACHE = 'embeddings_cache.npz'
X = None
if os.path.exists(CACHE):
    print(f'Loading cached embeddings from {CACHE}')
    with np.load(CACHE) as data:
        X_cached = data['X']
        cached_titles = data['titles'].tolist()
    if cached_titles == df['title'].tolist():
        X = X_cached
    else:
        print('Cache mismatch, recomputing.')
        os.remove(CACHE)

if X is None:
    print(f'Downloading encoder: {ENCODER_NAME} (~470MB, first time only)')
    from sentence_transformers import SentenceTransformer
    encoder = SentenceTransformer(ENCODER_NAME, device=str(device))
    print(f'Encoding {len(df)} titles...')
    X = encoder.encode(
        df['title'].tolist(),
        batch_size=BATCH_ENCODE,
        show_progress_bar=True,
        convert_to_numpy=True,
        normalize_embeddings=True,
    )
    np.savez_compressed(CACHE, X=X, titles=np.array(df['title'].tolist()))
    print(f'Cached embeddings to {CACHE}')

# Oversample real examples 100x — na numpy nivouu, bez ponovnog enkodovanja
real_mask = (df['source'] == 'real').values
if n_real > 0:
    real_idx = np.where(real_mask)[0]
    rep_idx = np.tile(real_idx, 100)
    X = np.concatenate([X, X[rep_idx]], axis=0)
    df = pd.concat([df] + [df.iloc[real_idx]] * 100, ignore_index=True)
    print(f'After oversampling real 100x: {len(df)} rows')

y = df['label'].values
assert X.shape == (len(df), EMB_DIM)

# ---------- 3) Per-class split (1 val example per class, rest train) ----------
# Standardni stratified fails jer 644 klasa > test_size. Radimo garantovan
# min-1-per-class val split, sto daje pouzdan macro eval.
val_idx = []
rng = np.random.default_rng(SEED)
for cls in range(num_classes):
    idxs = np.where(y == cls)[0]
    # Za klase sa >=2 primera - jedan u val. Za klase sa 1 primerom (ne desava se
    # kod nas jer imamo min 6 synth), preskoci val.
    if len(idxs) >= 2:
        val_idx.append(int(rng.choice(idxs, size=1)[0]))
val_idx = np.array(sorted(val_idx))
train_mask = np.ones(len(y), dtype=bool)
train_mask[val_idx] = False
train_idx = np.where(train_mask)[0]

X_train, X_val = X[train_idx], X[val_idx]
y_train, y_val = y[train_idx], y[val_idx]
print(f'Train: {len(X_train)}, Val: {len(X_val)} (1 per class where possible)')

# ---------- 4) Class weights (inverse freq) ----------
counts = np.bincount(y_train, minlength=num_classes)
counts = np.clip(counts, 1, None)
weights = (len(y_train) / (num_classes * counts)).astype(np.float32)
class_weights = torch.tensor(weights, device=device)

# ---------- 5) Model ----------
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


model = ClassifierHead(EMB_DIM, num_classes).to(device)
opt = optim.AdamW(model.parameters(), lr=LR, weight_decay=WEIGHT_DECAY)
sched = optim.lr_scheduler.CosineAnnealingLR(opt, T_max=EPOCHS)
criterion = nn.CrossEntropyLoss(weight=class_weights, label_smoothing=0.05)

X_train_t = torch.tensor(X_train, dtype=torch.float32)
y_train_t = torch.tensor(y_train, dtype=torch.long)
X_val_t = torch.tensor(X_val, dtype=torch.float32).to(device)
y_val_t = torch.tensor(y_val, dtype=torch.long).to(device)

train_loader = DataLoader(
    TensorDataset(X_train_t, y_train_t),
    batch_size=BATCH_TRAIN, shuffle=True,
)


def evaluate():
    model.eval()
    with torch.no_grad():
        logits = model(X_val_t)
        probs = torch.softmax(logits, dim=1)
        preds = probs.argmax(dim=1)
        top1 = (preds == y_val_t).float().mean().item()
        top3 = 0.0; top5 = 0.0
        for k, out in [(3, 'top3'), (5, 'top5')]:
            topk = probs.topk(k, dim=1).indices
            hit = (topk == y_val_t.unsqueeze(1)).any(dim=1).float().mean().item()
            if k == 3: top3 = hit
            else: top5 = hit
        loss = criterion(logits, y_val_t).item()
    return top1, top3, top5, loss


# ---------- 6) Train ----------
print('\nTraining...')
best_top3 = -1.0
best_state = None
patience_ct = 0

for epoch in range(1, EPOCHS + 1):
    model.train()
    total = 0.0
    for xb, yb in train_loader:
        xb, yb = xb.to(device), yb.to(device)
        opt.zero_grad()
        logits = model(xb)
        loss = criterion(logits, yb)
        loss.backward()
        opt.step()
        total += loss.item() * len(xb)
    sched.step()
    train_loss = total / len(X_train_t)

    top1, top3, top5, val_loss = evaluate()

    marker = ''
    if top3 > best_top3:
        best_top3 = top3
        best_state = {k: v.detach().cpu().clone() for k, v in model.state_dict().items()}
        patience_ct = 0
        marker = ' *'
    else:
        patience_ct += 1

    if epoch % 2 == 0 or epoch == 1 or marker:
        print(f'Ep {epoch:>3} | train_loss {train_loss:.4f} | val_loss {val_loss:.4f} | '
              f'top1 {top1*100:5.2f}% | top3 {top3*100:5.2f}% | top5 {top5*100:5.2f}%{marker}')

    if patience_ct >= PATIENCE:
        print(f'Early stop at epoch {epoch}')
        break

model.load_state_dict(best_state)
top1, top3, top5, val_loss = evaluate()

# ---------- 7) Per-source metrics ----------
val_df = df.iloc[val_idx].copy()
val_df['is_real'] = (val_df['source'] == 'real')

model.eval()
with torch.no_grad():
    logits = model(torch.tensor(X[val_idx], dtype=torch.float32).to(device))
    probs = torch.softmax(logits, dim=1)
    preds1 = probs.argmax(dim=1).cpu().numpy()
    preds3 = probs.topk(3, dim=1).indices.cpu().numpy()

val_df['pred1'] = preds1
val_df['correct1'] = (preds1 == val_df['label'].values)
val_df['correct3'] = [val_df['label'].values[i] in preds3[i] for i in range(len(val_df))]

real_v = val_df[val_df['is_real']]
synth_v = val_df[~val_df['is_real']]

report = []
report.append(f'Encoder: {ENCODER_NAME}')
report.append(f'Total training rows: {len(df)}')
report.append(f'  real: {(df["source"]=="real").sum()}, synthetic: {(df["source"]=="synthetic").sum()}')
report.append(f'Num classes: {num_classes}')
report.append(f'Train / Val split: {len(X_train)} / {len(X_val)}')
report.append('')
report.append('=== OVERALL VAL METRICS ===')
report.append(f'Top-1: {top1*100:.2f}%')
report.append(f'Top-3: {top3*100:.2f}%')
report.append(f'Top-5: {top5*100:.2f}%')
report.append('')
report.append('=== BY SOURCE (val subset) ===')
if len(real_v):
    report.append(f'REAL ({len(real_v)} val examples):')
    report.append(f'  Top-1: {real_v["correct1"].mean()*100:.2f}%')
    report.append(f'  Top-3: {real_v["correct3"].mean()*100:.2f}%')
if len(synth_v):
    report.append(f'SYNTHETIC ({len(synth_v)} val examples):')
    report.append(f'  Top-1: {synth_v["correct1"].mean()*100:.2f}%')
    report.append(f'  Top-3: {synth_v["correct3"].mean()*100:.2f}%')

report_str = '\n'.join(report)
print('\n' + report_str)

with open('training_report.txt', 'w', encoding='utf-8') as f:
    f.write(report_str)

# ---------- 8) Save artifacts ----------
torch.save({
    'state_dict': model.state_dict(),
    'in_dim': EMB_DIM,
    'num_classes': num_classes,
    'arch': 'mlp_384',
}, 'classifier_head.pth')

joblib.dump(le, 'label_encoder.pkl')

with open('encoder_model_name.txt', 'w', encoding='utf-8') as f:
    f.write(ENCODER_NAME)

# take-away: ostavimo i taxonomy id->name JSON za service-side pretty printing (opciono)
tax = pd.read_csv('taxonomy.csv')
name_by_id = {int(r['id']): r['name'] for _, r in tax.iterrows()}
with open('category_names.json', 'w', encoding='utf-8') as f:
    json.dump(name_by_id, f, ensure_ascii=False)

print('\nSaved: classifier_head.pth, label_encoder.pkl, encoder_model_name.txt, category_names.json, training_report.txt')
