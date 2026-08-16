"""
Generiše srpske naslove oglasa za kategorije koje nemaju ili imaju malo primera.

Čita missing_categories.txt i low_categories.txt, šalje GPT-4o-mini po 5 kategorija,
dobija 15 naslova po kategoriji. Dodaje u training_data.csv sa source='synthetic_sr'.

Cena: ~$0.05 ukupno.
Prerekvizit: OPENAI_API_KEY setovan
"""

import csv
import os
import time
from openai import OpenAI

TRAINING_DATA     = 'training_data.csv'
MISSING_FILE      = 'missing_categories.txt'
LOW_FILE          = 'low_categories.txt'
TARGET_PER_CAT    = 15
CATS_PER_BATCH    = 5

client = OpenAI()

SYSTEM_PROMPT = """Ti si ekspert za srpsko tržište iznajmljivanja opreme.
Generišeš kratke, realistične naslove oglasa za iznajmljivanje — takve kakve bi pravi korisnici pisali na srpskom sajtu za iznajmljivanje.

Pravila:
- Srpski jezik, latinica
- Kratko i jasno (2-7 reči)
- Različite formulacije: sa brendom, bez brenda, opisno, tehničke specifikacije
- Raznovrsno: ne samo "Iznajmljujem X" — i "X za iznajmljivanje", "X rent", samo naziv, sa modelom...
- Brendovi gde ima smisla: Makita, Bosch, Kärcher, Hilti, Stihl, JBL, Sony, DJI, Canon...

Format odgovora — TAČNO ovako, bez dodatnog teksta:
KAT_ID
naslov 1
naslov 2
...
naslov N
---
KAT_ID
naslov 1
...
---"""


def load_targets():
    targets = {}

    with open(MISSING_FILE, encoding='utf-8') as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            cid, name = line.split('|', 1)
            targets[cid] = {'name': name, 'need': TARGET_PER_CAT}

    if os.path.exists(LOW_FILE):
        with open(LOW_FILE, encoding='utf-8') as f:
            for line in f:
                line = line.strip()
                if not line:
                    continue
                parts = line.split('|')
                cid, name, count = parts[0], parts[1], int(parts[2])
                need = max(0, TARGET_PER_CAT - count)
                if need > 0 and cid not in targets:
                    targets[cid] = {'name': name, 'need': need}

    return targets


def generate_batch(batch: list[tuple[str, str, int]]) -> dict[str, list[str]]:
    """batch = [(cid, name, need), ...]"""
    user_lines = []
    for cid, name, need in batch:
        user_lines.append(f"{cid}\nKategorija: {name}\nBroj naslova: {need}")

    user_msg = "\n---\n".join(user_lines)

    for attempt in range(3):
        try:
            resp = client.chat.completions.create(
                model='gpt-4o-mini',
                messages=[
                    {'role': 'system', 'content': SYSTEM_PROMPT},
                    {'role': 'user',   'content': user_msg},
                ],
                temperature=0.7,
            )
            text = resp.choices[0].message.content.strip()
            return parse_response(text, batch)
        except Exception as e:
            print(f"  API greška (pokušaj {attempt+1}): {e}")
            time.sleep(2 ** attempt)
    return {}


def parse_response(text: str, batch: list) -> dict[str, list[str]]:
    result = {}
    expected_ids = {b[0] for b in batch}
    current_id = None
    current_titles = []

    for line in text.split('\n'):
        line = line.strip()
        if not line:
            continue
        if line == '---':
            if current_id and current_titles:
                result[current_id] = current_titles
            current_id = None
            current_titles = []
        elif line in expected_ids:
            if current_id and current_titles:
                result[current_id] = current_titles
            current_id = line
            current_titles = []
        elif current_id:
            # Ukloni numeraciju ako postoji (npr. "1. naslov")
            if len(line) > 2 and line[0].isdigit() and line[1] in '.):':
                line = line[2:].strip()
            if line:
                current_titles.append(line)

    if current_id and current_titles:
        result[current_id] = current_titles

    return result


def main():
    print("Učitavanje ciljnih kategorija...")
    targets = load_targets()
    print(f"  Missing: {sum(1 for t in targets.values() if t['need'] == TARGET_PER_CAT)}")
    print(f"  Low (need top-up): {sum(1 for t in targets.values() if t['need'] < TARGET_PER_CAT)}")
    print(f"  Ukupno kategorija: {len(targets)}")

    items = [(cid, t['name'], t['need']) for cid, t in targets.items()]
    batches = [items[i:i+CATS_PER_BATCH] for i in range(0, len(items), CATS_PER_BATCH)]
    print(f"  API poziva: {len(batches)}")

    all_new = []
    failed = []

    for i, batch in enumerate(batches):
        result = generate_batch(batch)
        for cid, name, need in batch:
            titles = result.get(cid, [])
            if not titles:
                failed.append(f"{cid}: {name}")
                continue
            for title in titles[:need]:
                all_new.append({'title': title, 'category_id': cid, 'source': 'synthetic_sr'})

        if (i + 1) % 10 == 0 or i == len(batches) - 1:
            print(f"  [{i+1}/{len(batches)}] — {len(all_new)} naslova generisano")

    # Upisati u training_data.csv
    with open(TRAINING_DATA, encoding='utf-8', errors='replace') as f:
        existing = list(csv.DictReader(f))

    # Ukloni stare synthetic_sr redove i dodaj nove
    existing = [r for r in existing if r['source'] != 'synthetic_sr']
    all_rows = existing + all_new

    with open(TRAINING_DATA, 'w', newline='', encoding='utf-8') as f:
        w = csv.DictWriter(f, fieldnames=['title', 'category_id', 'source'])
        w.writeheader()
        w.writerows(all_rows)

    real_count    = sum(1 for r in all_rows if r['source'] == 'real')
    hygglo_count  = sum(1 for r in all_rows if r['source'] == 'hygglo')
    synth_count   = sum(1 for r in all_rows if r['source'] == 'synthetic_sr')

    print(f"\ntraining_data.csv:")
    print(f"  real:         {real_count}")
    print(f"  hygglo (SR):  {hygglo_count}")
    print(f"  synthetic_sr: {synth_count}")
    print(f"  UKUPNO:       {len(all_rows)}")

    if failed:
        print(f"\nNisu generisani ({len(failed)}):")
        for f in failed:
            print(f"  {f}")

    print("\nPokreni: python train_model.py")


if __name__ == '__main__':
    main()
