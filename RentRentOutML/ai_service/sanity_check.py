"""Prošireni sanity check — 200 realnih srpskih upita pokriva sve glavne kategorije."""
import sys, io, json, torch, torch.nn as nn, joblib
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


def predict(title, k=3):
    with torch.no_grad():
        emb = encoder.encode([title], convert_to_tensor=True, normalize_embeddings=True, show_progress_bar=False)
        probs = torch.softmax(head(emb), dim=1)[0]
        top = torch.topk(probs, k)
    top1_cid = int(le.inverse_transform([top.indices[0].item()])[0])
    top1_name = names.get(top1_cid, '?')
    top1_conf = top.values[0].item() * 100
    others = []
    for i in range(1, k):
        cid = int(le.inverse_transform([top.indices[i].item()])[0])
        others.append(f"{names.get(cid,'?')} ({top.values[i].item()*100:.0f}%)")
    print(f"  {top1_conf:5.1f}%  {top1_name:<35}  | {', '.join(others)}")


TESTS = [
    # ── ALATI ──────────────────────────────────────────────────────
    "⚙ ALATI",
    "akumulatorska bušilica",
    "Makita akumulatorska bušilica",
    "Bosch bušilica",
    "Hilti bušilica",
    "udarna bušilica",
    "ugaona brusilica",
    "Makita brusilica 125mm",
    "kružna testera",
    "ubodna testera",
    "sabljasta testera",
    "motorna testera Stihl",
    "vibraciona brusilica",
    "pneumatski čekić",
    "sekač betona",
    "kompressor za vazduh",
    "kompresor 200l",
    "mešalica za beton",
    "aparat za zavarivanje",
    "inverter za zavarivanje",
    "laser za nivelaciju",
    "rotirajući laser",
    "libela",
    "merdevine aluminijumske",
    "prskalica za boju",
    "elektricni rendiser",
    "duvač lišća",
    "makaze za živicu",
    "električna makaza za živicu",
    "Husqvarna kosilica",
    "kosilica za travu",
    "traktorska kosilica",
    "trimmer za travu",
    "aeroter za travnjak",
    "rezač pločica",
    "brusilica za podove",
    # ── ČIŠĆENJE ───────────────────────────────────────────────────
    "⚙ ČIŠĆENJE",
    "karcher",
    "Kärcher visokopritisnipresac",
    "mašina za pranje pod pritiskom",
    "perač pod pritiskom",
    "usisivač građevinski",
    "industrijski usisivač",
    "mašina za pranje tepiha",
    "mašina za poliranje podova",
    # ── GRAĐEVINA ──────────────────────────────────────────────────
    "🏗 GRAĐEVINA",
    "skela",
    "skela za fasadu",
    "mini bager",
    "bager rovokopač",
    "bobcat",
    "viljuškar",
    "pumpa za beton",
    "vibrator za beton",
    "agregat struja",
    "generator struje",
    "potapajuća pumpa",
    "pumpa za vodu",
    "grejač gradilišta",
    "damper",
    # ── KAMERA I VIDEO ─────────────────────────────────────────────
    "📷 KAMERA",
    "Sony FX3",
    "Sony FX3 kamera",
    "Sony A7 IV",
    "Sony A7S III",
    "Canon R6",
    "Canon EOS R",
    "Blackmagic kamera",
    "RED kamera",
    "DJI Ronin gimbal",
    "gimbal stabilizator",
    "kamera za snimanje",
    "objektiv 24-70mm",
    "projektor",
    "projektor za film",
    "ring svetlo",
    "studijsko osvetljenje",
    "LED panel osvetljenje",
    "GoPro kamera",
    "akciona kamera",
    "360 kamera",
    # ── DRONOVI ────────────────────────────────────────────────────
    "🚁 DRONOVI",
    "DJI Mavic",
    "DJI drone",
    "dron za snimanje",
    "DJI Mini 3 Pro",
    # ── AUDIO ──────────────────────────────────────────────────────
    "🔊 AUDIO",
    "zvučnici za žurku",
    "JBL zvučnici",
    "Mackie zvučnici",
    "PA sistem",
    "bezični mikrofon",
    "mikrofon Sennheiser",
    "DJ oprema",
    "DJ kontroler",
    "muzički stub",
    "RØDE mikrofon",
    # ── DOGAĐAJI ───────────────────────────────────────────────────
    "🎪 DOGAĐAJI",
    "šator za svadbu",
    "šator za proslave",
    "šator za 100 gostiju",
    "bina za nastup",
    "sto i stolice za svadbu",
    "dekoracija za venčanje",
    "mašina za dim",
    "konfeti top",
    "LED rasveta za žurku",
    "aparat za kokice",
    "slomašina za šećernu vunu",
    # ── ODELA I KOSTIMI ────────────────────────────────────────────
    "👗 ODELA",
    "venčanica",
    "smoking",
    "kostim za Halloween",
    "haljina za matursku",
    # ── STANOVI ────────────────────────────────────────────────────
    "🏠 STANOVI",
    "stan na dan Beograd",
    "stan na dan Novi Sad",
    "apartman centar Beograd",
    "vikendica iznajmljivanje",
    "kuća za odmor",
    "soba za iznajmljivanje",
    # ── VOZILA ─────────────────────────────────────────────────────
    "🚗 VOZILA",
    "prikolica",
    "kamp prikolica",
    "kombi za selidbu",
    "auto za venčanje",
    "motor za iznajmljivanje",
    "električni romobil",
    # ── BICIKLI ────────────────────────────────────────────────────
    "🚲 BICIKLI",
    "planinski bicikl",
    "električni bicikl",
    "e-bicikl",
    "bicikl za decu",
    # ── VODENI SPORTOVI ────────────────────────────────────────────
    "🚣 VODA",
    "kajak",
    "SUP daska",
    "sup daska za more",
    "kanu",
    "gumeni čamac",
    "jedrilica",
    "jet ski",
    "kitbording oprema",
    "windsurfing",
    # ── ZIMSKI SPORTOVI ────────────────────────────────────────────
    "⛷ ZIMSKI",
    "skije",
    "ski čizme",
    "ski oprema komplet",
    "snoubord",
    "saonice",
    # ── PLANINARENJE ───────────────────────────────────────────────
    "🏕 PLANINARENJE",
    "šator za kampovanje",
    "vreća za spavanje",
    "ruksak za planinarenje",
    "oprema za penjanje",
    # ── RIBOLOV ────────────────────────────────────────────────────
    "🎣 RIBOLOV",
    "ribolov štap",
    "ribolov oprema komplet",
    # ── GAMING ─────────────────────────────────────────────────────
    "🎮 GAMING",
    "PlayStation 5",
    "PS5",
    "VR naočare",
    "Xbox",
    # ── TECH ───────────────────────────────────────────────────────
    "💻 TECH",
    "laptop za iznajmljivanje",
    "iPad",
    "3D štampač",
    "TV ekran za iznajmljivanje",
    "foto kabina",
    # ── BEBA ───────────────────────────────────────────────────────
    "👶 BEBA",
    "kolica za bebu",
    "auto sedište za bebu",
    "krevetić za bebu",
    # ── MEDICINSKA ─────────────────────────────────────────────────
    "🏥 MEDICINSKA",
    "invalidska kolica",
    "štake",
    # ── PARTY ──────────────────────────────────────────────────────
    "🎉 PARTY",
    "bouncy castle",
    "skakavac za decu",
    "luna park oprema",
    "bumper ball",
    # ── OSTALO ─────────────────────────────────────────────────────
    "📦 OSTALO",
    "iphone",
    "samsung telefon",
    "frižider",
    "veš mašina",
    "klima uređaj",
    "muzički instrument",
    "gitara",
    "klavir",
]

print("\n" + "="*75)
print(f"{'UPIT':<35}  {'TOP-1 PREDIKCIJA':<35}  TOP-2, TOP-3")
print("="*75)

for item in TESTS:
    if item.startswith(('⚙', '🏗', '📷', '🚁', '🔊', '🎪', '👗', '🏠', '🚗',
                        '🚲', '🚣', '⛷', '🏕', '🎣', '🎮', '💻', '👶', '🏥',
                        '🎉', '📦')):
        print(f"\n{item}")
        continue
    print(f"  {item:<33}", end='')
    predict(item, k=3)
