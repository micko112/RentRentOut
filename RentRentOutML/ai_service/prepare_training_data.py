"""
Kombinuje realne labeled oglase (labeled_ads.csv) sa sintetickim primerima.

Strategija:
- Real: koristi sve non-SKIP labele (280).
- Synthetic: 6 primera po leaf kategoriji radi popunjavanja svih 644 klasa
  i dodatne raznolikosti (~3864).
- Class weighting radimo u treningu (ne oversample-ujemo ovde).

Izlaz: training_data.csv sa kolonama: title, category_id, source
       (source = 'real' | 'synthetic')
"""
import sys, io, csv, random, re, unicodedata
import pandas as pd

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
random.seed(42)

# ---------- 1) Ucitaj taksonomiju ----------
tax = pd.read_csv('taxonomy.csv')
leafs = tax[tax['is_leaf'] == 1].copy()
leaf_ids = set(leafs['id'].tolist())
name_by_id = dict(zip(tax['id'], tax['name']))
parent_by_id = dict(zip(tax['id'], tax['parent_id']))


def find_root(cat_id):
    current = cat_id
    while pd.notna(parent_by_id.get(current)) and parent_by_id.get(current):
        current = int(parent_by_id[current])
    return current


# ---------- 2) Ucitaj realne labele ----------
real = pd.read_csv('labeled_ads.csv')
real = real[real['category_id'] != 'SKIP'].copy()
real['category_id'] = real['category_id'].astype(int)
assert real['category_id'].isin(leaf_ids).all(), 'Neki real category_id nije leaf'
real_out = real[['title', 'category_id']].copy()
real_out['source'] = 'real'
print(f'Real labeled: {len(real_out)}')

# ---------- 3) Generisi sinteticke ----------
BRAND_MAP = {
    100: ['Bosch', 'Makita', 'Hilti', 'DeWalt', 'Einhell', 'Wurth'],
    200: ['Sony', 'Samsung', 'Apple', 'LG', 'Philips', 'Asus', 'HP', 'DJI'],
    300: ['Canon', 'Nikon', 'Sony', 'Panasonic', 'GoPro', 'DJI', 'Godox'],
    400: ['Stihl', 'Husqvarna', 'Villager', 'Honda', 'Gardena'],
    500: ['Karcher', 'Dyson', 'Gorenje', 'Beko', 'Bosch', 'Miele'],
    600: ['JBL', 'Pioneer', 'Yamaha', 'Bose'],
    700: ['Kettler', 'Capriolo', 'Yamaha', 'Polar', 'Salomon', 'Burton', 'Thule'],
    800: ['Thule', 'Yamaha', 'Bockmann', 'BMW', 'Audi', 'VW'],
    900: ['Lux', 'Premium', 'Standard'],
}
PREFIXES = ['Iznajmljujem', 'Izdajem', 'Rentiranje:', 'Povoljno', 'Hitno', 'Novo:',
            'Na zajam', 'Najam', 'Najam/Iznajmljivanje', '']
SUFFIXES = ['na dan', 'za vikend', 'povoljno', 'sa dostavom', 'Beograd', 'Novi Sad',
            'akcija', '- izdavanje', '', '']

SAMPLES_PER_LEAF = 6
synth_rows = []
for _, row in leafs.iterrows():
    cid = int(row['id'])
    name = row['name'].lower()
    root = find_root(cid)
    brands = BRAND_MAP.get(root, ['Kvalitetno', 'Profi'])

    for _ in range(SAMPLES_PER_LEAF):
        prefix = random.choice(PREFIXES)
        suffix = random.choice(SUFFIXES)
        brand = random.choice(brands)

        structure = random.choice([
            f'{prefix} {brand} {name} {suffix}',
            f'{brand} {name} {suffix}',
            f'{prefix} {name} {brand}',
            f'{name} {brand} {suffix}',
            f'{prefix} {name}',
            f'{name} - {suffix}',
        ])
        title = ' '.join(structure.split())
        synth_rows.append({'title': title, 'category_id': cid, 'source': 'synthetic'})

# ---------- 3b) SYNONYM / brand-name specific examples ----------
# Za svaki alias generisemo par varijacija (sam alias, sa prefixom, sa sufiksom).
# Ovo eksplicitno uci model da povezuje brend/kolokvijalni naziv sa leaf kategorijom.
SYNONYMS = {
    1322: ['karcher', 'karcer', 'kercer', 'perac pod pritiskom', 'perac pritiskom',
           'visokopritisni perac', 'masina za pranje pod pritiskom'],
    1161: ['iphone', 'samsung galaxy', 'smartphone', 'mobilni telefon', 'huawei',
           'xiaomi mobilni', 'redmi'],
    1154: ['playstation 5', 'ps5', 'ps4', 'xbox series x', 'xbox one',
           'nintendo switch', 'konzola za igre'],
    1181: ['udarna busilica', 'hilti busilica', 'bosch busilica', 'makita busilica',
           'busilica akumulatorska', 'akumulatorska busilica'],
    1177: ['pikamer', 'stemalica', 'stemarka'],
    1185: ['rotacioni cekic', 'sds cekic', 'hilti cekic', 'busilica cekic',
           'kombinovana busilica'],
    1024: ['agregat', 'generator', 'agregat za struju', 'honda agregat',
           'benzinski agregat', 'diesel agregat'],
    1191: ['kompresor', 'vazdusni kompresor', 'kompresor za vazduh',
           'atlas copco kompresor'],
    1568: ['skije', 'pancerice', 'snowboard', 'skijaska oprema', 'zimska oprema'],
    1474: ['gitara', 'elektricna gitara', 'akusticna gitara', 'fender', 'gibson'],
    1139: ['dji mavic', 'mavic 3', 'mavic pro', 'mavic air'],
    1136: ['dji fpv', 'fpv dron'],
    1138: ['dji phantom', 'phantom 4'],
    1130: ['prenosni zvucnik', 'jbl zvucnik', 'sony zvucnik', 'bluetooth zvucnik',
           'jbl party box', 'jbl xtreme'],
    1148: ['laptop', 'macbook', 'thinkpad', 'dell laptop', 'hp laptop', 'lenovo laptop'],
    1254: ['dslr', 'canon dslr', 'nikon dslr', 'canon 5d', 'nikon d850'],
    1257: ['mirrorless', 'sony a7', 'canon r5', 'sony alpha'],
    1341: ['kosilica', 'kosacica', 'kosilica za travu', 'stihl kosilica'],
    1309: ['motorna testera', 'stihl motorka', 'motorka za drvo'],
    1310: ['trimer', 'trimer za travu', 'stihl trimer'],
    1459: ['sator za svadbu', 'sator za proslavu', 'pagoda', 'pagoda sator'],
    1425: ['haljina', 'vencanica', 'bal haljina', 'party odeca'],
    1428: ['bar sto', 'barski sto', 'koktel sto', 'visoki sto'],
    1429: ['stolica za svadbu', 'stolica za proslavu', 'chiavari stolica',
           'medaljon stolica'],
    1546: ['sup daska', 'paddleboard', 'sup', 'stand up paddle'],
    1193: ['makazasta platforma', 'makazasta dizalica', 'platforma za rad na visini',
           'skila platforma'],
    1232: ['gradjevinska skela', 'skela', 'pokretna skela', 'aluminijumska skela'],
    1235: ['podupirac', 'gradjevinski podupirac'],
    1208: ['vibro nabijac', 'vibro ploca', 'vibro valjak'],
    1369: ['dubinsko pranje', 'dubinsko pranje tepiha', 'cistac tepiha',
           'karcher tepih'],
    1152: ['arkadna masina', 'pinball', 'arkada'],
    1374: ['parocistac', 'parni cistac', 'parna masina za ciscenje'],
    1147: ['grafička tabla', 'wacom', 'graficka tabla'],
    1451: ['sladoled masina', 'granita', 'slush masina'],
    1454: ['kokicar', 'masina za kokice'],
    1448: ['secerna vuna', 'masina za secernu vunu'],
    1416: ['masina za balone', 'baloni'],
    1418: ['masina za dim', 'dim masina'],
    1417: ['party rasveta', 'diskoteka rasveta', 'led par'],
    1587: ['jump starter', 'starter baterija', 'auto punjac'],
    1595: ['krovni kofer', 'krovna kutija', 'thule krovni kofer'],
    1597: ['krovni nosac', 'thule nosac'],
    1583: ['prva pomoc', 'defibrilator', 'cpr trening'],
    1584: ['hodalica', 'walker'],
    1585: ['invalidska kolica'],
}

for cid, aliases in SYNONYMS.items():
    if cid not in leaf_ids:
        continue
    root = find_root(cid)
    brands = BRAND_MAP.get(root, ['Profi'])
    for alias in aliases:
        # 3 varijacije po sinonimu: gol, sa prefixom, sa suffixom
        synth_rows.append({'title': alias, 'category_id': cid, 'source': 'synthetic'})
        synth_rows.append({
            'title': f'{random.choice(PREFIXES)} {alias} {random.choice(SUFFIXES)}'.strip(),
            'category_id': cid, 'source': 'synthetic',
        })
        synth_rows.append({
            'title': f'{alias} {random.choice(brands)}',
            'category_id': cid, 'source': 'synthetic',
        })

synth_out = pd.DataFrame(synth_rows)
print(f'Synthetic generated: {len(synth_out)} ({SAMPLES_PER_LEAF} per {len(leafs)} leaf classes)')

# ---------- 4) Merge & shuffle ----------
combined = pd.concat([real_out, synth_out], ignore_index=True)
combined = combined.sample(frac=1, random_state=42).reset_index(drop=True)
combined.to_csv('training_data.csv', index=False, encoding='utf-8')

print(f'\nTotal training rows: {len(combined)}')
print(f'  real:      {(combined["source"] == "real").sum()}')
print(f'  synthetic: {(combined["source"] == "synthetic").sum()}')
print(f'Unique leaf classes with any data: {combined["category_id"].nunique()} / {len(leafs)}')

per_class = combined.groupby('category_id').size()
print(f'\nExamples-per-class stats:')
print(f'  min:    {per_class.min()}')
print(f'  median: {int(per_class.median())}')
print(f'  max:    {per_class.max()}')

top_real = real_out['category_id'].value_counts().head(10)
print('\nTop 10 real-data classes:')
for cid, n in top_real.items():
    print(f'  {n:>3}x  {name_by_id.get(cid, "?")} ({cid})')
