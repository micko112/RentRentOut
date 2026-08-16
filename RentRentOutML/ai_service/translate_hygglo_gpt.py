"""
Prevodi 87k Hygglo naslova EN → SR koristeći GPT-4o-mini.
Koristi srpsku tržišnu terminologiju (ne doslovni prevod).
Checkpoint svakih 100 batch-eva — bezbedno prekinuti i nastaviti.

Cena: ~$0.50 za svih 87k naslova.

Prerekvizit: pip install openai tqdm
Pokretanje:  OPENAI_API_KEY=sk-... python translate_hygglo_gpt.py
             ili setuj OPENAI_API_KEY u .env i pokreni
"""

import csv
import os
import time

from openai import OpenAI
from tqdm import tqdm

SOURCE_CSV   = 'Hygglo_Ultimativna_Baza_18-16-12.csv'
CATEGORY_MAP = 'hygglo_category_map.csv'
TRAINING_DATA = 'training_data.csv'
CHECKPOINT   = 'hygglo_sr_checkpoint.csv'

MIN_SIMILARITY = 0.40
BATCH_SIZE     = 50
SAVE_EVERY     = 100   # batch-eva između checkpoint-a (~5k naslova)

SYSTEM_PROMPT = """Ti si prevodilac za srpsko tržište iznajmljivanja.
Prevedi svaki naslov opreme sa engleskog na srpski koristeći termine koji se ZAPRAVO koriste u srpskim oglasima.

Pravila:
- Srpska tržišna terminologija, NE doslovni prevod
- Brendovi ostaju nepromenjeni (Makita, Bosch, Kärcher, DJI, Sony, Canon, Husqvarna...)
- Kratko i jasno — idealno 2–6 reči
- Latinica, bez dijakritičkih grešaka

Primeri ispravnih prevoda:
Cordless drill → Akumulatorska bušilica
Pressure washer → Mašina za pranje pod pritiskom
Lawn mower → Kosilica za travu
Ride-on lawn mower → Traktorska kosilica
Circular saw → Kružna testera
Angle grinder → Ugaona brusilica
Jigsaw → Ubodna testera
Reciprocating saw → Sabljasta testera
Scaffolding → Skela
Cement mixer → Mešalica za beton
Generator → Agregat
Chainsaw → Motorna testera
Leaf blower → Duvač lišća
Tile cutter → Rezač pločica
Welding machine → Aparat za zavarivanje
Excavator → Mini bager
Trailer → Prikolica
Projector → Projektor
Drone → Dron
Wedding dress → Venčanica
Tent → Šator
Kayak → Kajak
Stand-up paddleboard → SUP daska
Ski boots → Ski čizme
Tuxedo → Smoking

Vrati ISKLJUČIVO prevedene naslove, svaki u novom redu.
ISTI BROJ redova kao u unosu, ISTI REDOSLED. Bez numeracije, bez objašnjenja."""

client = OpenAI()

_SWEDISH = str.maketrans({'Ä': 'A', 'ä': 'a', 'Å': 'A', 'å': 'a', 'Ö': 'O', 'ö': 'o'})


def _clean(text: str) -> str:
    return text.translate(_SWEDISH)


# ── Učitavanje ─────────────────────────────────────────────────────────────────

def load_category_map():
    mapping, sim = {}, {}
    with open(CATEGORY_MAP, encoding='utf-8-sig') as f:
        reader = csv.DictReader(f)
        id_col = reader.fieldnames[0]
        for row in reader:
            hid = row[id_col]
            override = row.get('override_category_id', '').strip()
            mapping[hid] = override if override else row['matched_category_id']
            sim[hid] = float(row['similarity'])
    return mapping, sim


def load_unique_products(cat_map, sim_scores):
    seen, products = set(), []
    with open(SOURCE_CSV, encoding='utf-8') as f:
        for row in csv.DictReader(f):
            pid = row['product.id']
            if pid in seen:
                continue
            seen.add(pid)
            hid  = row['product.category.id']
            name = row['product.name'].strip()
            if not name or hid not in cat_map:
                continue
            if sim_scores.get(hid, 0) < MIN_SIMILARITY:
                continue
            products.append({
                'product_id':  pid,
                'title_en':    name,
                'category_id': cat_map[hid],
            })
    return products


def load_checkpoint():
    if not os.path.exists(CHECKPOINT):
        return set()
    done = set()
    with open(CHECKPOINT, encoding='utf-8') as f:
        for row in csv.DictReader(f):
            done.add(row['product_id'])
    return done


# ── Prevod ─────────────────────────────────────────────────────────────────────

def _call_api(titles: list[str]) -> list[str] | None:
    """Jedan API poziv; vraća listu prevoda ili None ako count ne štima."""
    resp = client.chat.completions.create(
        model='gpt-4o-mini',
        messages=[
            {'role': 'system', 'content': SYSTEM_PROMPT},
            {'role': 'user',   'content': '\n'.join(titles)},
        ],
        temperature=0.2,
    )
    lines = [l.strip() for l in resp.choices[0].message.content.strip().split('\n') if l.strip()]
    return lines if len(lines) == len(titles) else None


def translate_batch(titles: list[str]) -> list[str]:
    """Prevodi batch; u slučaju greške ili count mismatch-a — fallback jedan po jedan."""
    for attempt in range(2):
        try:
            result = _call_api(titles)
            if result:
                return result
        except Exception as e:
            print(f"\n  API greška (pokušaj {attempt+1}): {e}")
            time.sleep(2 ** attempt)

    # Fallback: svaki naslov posebno
    out = []
    for t in titles:
        for attempt in range(3):
            try:
                r = _call_api([t])
                out.append(r[0] if r else t)
                break
            except Exception as e:
                if attempt == 2:
                    out.append(t)  # vrati original ako sve padne
                time.sleep(2 ** attempt)
    return out


# ── Checkpoint I/O ─────────────────────────────────────────────────────────────

FIELDS = ['product_id', 'title_sr', 'category_id']


def flush(buffer: list[dict], first: bool):
    mode = 'w' if first else 'a'
    with open(CHECKPOINT, mode, newline='', encoding='utf-8') as f:
        w = csv.DictWriter(f, fieldnames=FIELDS)
        if first:
            w.writeheader()
        w.writerows(buffer)


# ── Rebuild training_data.csv ──────────────────────────────────────────────────

def rebuild_training_data():
    with open(TRAINING_DATA, encoding='utf-8', errors='replace') as f:
        existing = [r for r in csv.DictReader(f) if r['source'] not in ('hygglo',)]

    new_rows = []
    with open(CHECKPOINT, encoding='utf-8') as f:
        for row in csv.DictReader(f):
            sr = row['title_sr'].strip()
            if sr:
                new_rows.append({'title': sr, 'category_id': row['category_id'], 'source': 'hygglo'})

    all_rows = existing + new_rows
    with open(TRAINING_DATA, 'w', newline='', encoding='utf-8') as f:
        w = csv.DictWriter(f, fieldnames=['title', 'category_id', 'source'])
        w.writeheader()
        w.writerows(all_rows)

    print(f"training_data.csv: {len(all_rows)} redova  "
          f"({len(existing)} real + {len(new_rows)} hygglo SR)")


# ── Main ───────────────────────────────────────────────────────────────────────

def main():
    print("Učitavanje mape kategorija...")
    cat_map, sim_scores = load_category_map()

    print("Čitanje Hygglo CSV-a...")
    products = load_unique_products(cat_map, sim_scores)
    print(f"Prihvaćeno: {len(products)} jedinstvenih proizvoda")

    done_ids   = load_checkpoint()
    remaining  = [p for p in products if p['product_id'] not in done_ids]
    print(f"Već prevedeno: {len(done_ids)},  ostalo: {len(remaining)}")

    if remaining:
        avg_tok  = 10
        est_cost = len(remaining) * avg_tok * 2 / 1_000_000 * 0.60
        print(f"Procenjena cena: ~${est_cost:.2f}\n")

        batches     = [remaining[i:i+BATCH_SIZE] for i in range(0, len(remaining), BATCH_SIZE)]
        buffer      = []
        first_write = len(done_ids) == 0
        translated  = len(done_ids)

        for i, batch in enumerate(tqdm(batches, desc="EN→SR")):
            titles      = [p['title_en'] for p in batch]
            translated_titles = translate_batch(titles)

            for p, sr in zip(batch, translated_titles):
                buffer.append({
                    'product_id':  p['product_id'],
                    'title_sr':    _clean(sr),
                    'category_id': p['category_id'],
                })

            translated += len(batch)

            if (i + 1) % SAVE_EVERY == 0 or i == len(batches) - 1:
                flush(buffer, first=first_write)
                first_write = False
                buffer      = []
                pct = translated / len(products) * 100
                tqdm.write(f"  checkpoint [{translated}/{len(products)}  {pct:.0f}%]")

        print("\nPrevod završen!")

    print("\nRebuildujem training_data.csv...")
    rebuild_training_data()
    print("\nSledeći korak: python train_model.py")


if __name__ == '__main__':
    main()
