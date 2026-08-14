"""
Direktno izvlači engleske naslove iz Hygglo CSV-a i primenjuje ispravljenu
mapu kategorija. Bez prevoda — multilingual encoder sam mapira EN↔SR.

Zamena za step2_translate.py + step3_finalize.py.
"""

import csv
from collections import Counter

SOURCE_CSV = 'Hygglo_Ultimativna_Baza_18-16-12.csv'
CATEGORY_MAP = 'hygglo_category_map.csv'
TRAINING_DATA = 'training_data.csv'
MIN_SIMILARITY = 0.40


def load_category_map():
    mapping = {}
    sim = {}
    with open(CATEGORY_MAP, encoding='utf-8-sig') as f:
        reader = csv.DictReader(f)
        id_col = reader.fieldnames[0]
        for row in reader:
            hid = row[id_col]
            override = row.get('override_category_id', '').strip()
            mapping[hid] = override if override else row['matched_category_id']
            sim[hid] = float(row['similarity'])
    return mapping, sim


def main():
    print("Učitavanje mape kategorija...")
    cat_map, sim_scores = load_category_map()

    print("Čitanje Hygglo CSV-a (dedupliciranje po product.id)...")
    seen = set()
    results = []
    skipped_sim = 0
    skipped_dup = 0

    with open(SOURCE_CSV, encoding='utf-8') as f:
        for row in csv.DictReader(f):
            pid = row['product.id']
            if pid in seen:
                skipped_dup += 1
                continue
            seen.add(pid)

            hid = row['product.category.id']
            title = row['product.name'].strip()

            if not title or hid not in cat_map:
                continue

            if sim_scores.get(hid, 0) < MIN_SIMILARITY:
                skipped_sim += 1
                continue

            results.append({
                'title': title,
                'category_id': cat_map[hid],
                'source': 'hygglo',
            })

    print(f"Prihvaćeno:              {len(results):>7}")
    print(f"Preskočeno (duplikati):  {skipped_dup:>7}")
    print(f"Preskočeno (niska sim):  {skipped_sim:>7}")
    print(f"Jedinstvenih kategorija: {len(set(r['category_id'] for r in results)):>7}")

    # Obriši stare Hygglo redove iz training_data.csv
    with open(TRAINING_DATA, encoding='utf-8') as f:
        existing = [r for r in csv.DictReader(f) if r['source'] != 'hygglo']
    print(f"\nPostojeći realni redovi: {len(existing)}")

    # Dodaj nove
    all_rows = existing + results
    with open(TRAINING_DATA, 'w', newline='', encoding='utf-8') as f:
        writer = csv.DictWriter(f, fieldnames=['title', 'category_id', 'source'])
        writer.writeheader()
        writer.writerows(all_rows)

    print(f"Ukupno u training_data.csv: {len(all_rows)}")
    print("\nPokreni: python train_model.py")


if __name__ == '__main__':
    main()
