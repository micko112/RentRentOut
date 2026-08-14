"""
Korak 3: Kombinuje prevedene naslove + mapu kategorija u training_data format.

Filtrira po pragu sličnosti i opciono dodaje u training_data.csv za retreniranje.
"""

import csv
from collections import Counter

TRANSLATED_FILE = 'hygglo_translated_checkpoint.csv'
CATEGORY_MAP_FILE = 'hygglo_category_map.csv'
TRAINING_DATA_FILE = 'training_data.csv'
OUTPUT_FILE = 'hygglo_training_data.csv'

# Prag sličnosti — pošto kategorije dolaze sa Hyggla, 0.40 je dovoljno konzervativno.
# Spusti na 0.30 za više podataka, podigni na 0.50 za veći kvalitet.
MIN_SIMILARITY = 0.40


def load_category_map():
    mapping = {}
    sim_scores = {}
    with open(CATEGORY_MAP_FILE, encoding='utf-8-sig') as f:
        reader = csv.DictReader(f)
        id_col = reader.fieldnames[0]  # prva kolona, bez obzira na ime
        for row in reader:
            hid = row[id_col]
            override = row.get('override_category_id', '').strip()
            cat_id = override if override else row['matched_category_id']
            mapping[hid] = cat_id
            sim_scores[hid] = float(row['similarity'])
    return mapping, sim_scores


def main():
    print("Učitavanje mape kategorija...")
    cat_map, sim_scores = load_category_map()

    print("Kombinovanje podataka...")
    results = []
    skipped_sim = 0
    skipped_empty = 0

    with open(TRANSLATED_FILE, encoding='utf-8') as f:
        for row in csv.DictReader(f):
            hid = row['hygglo_category_id']
            title_sr = row['title_sr'].strip()

            if not title_sr or len(title_sr) < 3:
                skipped_empty += 1
                continue

            if hid not in cat_map:
                skipped_empty += 1
                continue

            if sim_scores.get(hid, 0) < MIN_SIMILARITY:
                skipped_sim += 1
                continue

            results.append({
                'title': title_sr,
                'category_id': cat_map[hid],
                'source': 'hygglo',
            })

    with open(OUTPUT_FILE, 'w', newline='', encoding='utf-8') as f:
        writer = csv.DictWriter(f, fieldnames=['title', 'category_id', 'source'])
        writer.writeheader()
        writer.writerows(results)

    cat_dist = Counter(r['category_id'] for r in results)

    print(f"\n=== Rezultat ===")
    print(f"  Prihvaćeno:                    {len(results):>7}")
    print(f"  Preskočeno (niska sim <{MIN_SIMILARITY}):  {skipped_sim:>7}")
    print(f"  Preskočeno (prazno/nepoznato): {skipped_empty:>7}")
    print(f"  Jedinstvenih kategorija:       {len(cat_dist):>7}")

    # Koliko kategorija nema ni jedan primer
    all_cat_ids = set()
    with open('taxonomy.csv', encoding='utf-8') as f:
        for row in csv.DictReader(f):
            if row['is_leaf'] == '1':
                all_cat_ids.add(row['id'])
    missing = all_cat_ids - set(cat_dist.keys())
    print(f"  Kategorije bez ijednog primera: {len(missing)}")
    if missing:
        print(f"  (ove kategorije ce imati samo originalnih {sum(1 for _ in open('training_data.csv', encoding='utf-8'))-1} realnih primera)")

    print(f"\nSačuvano: {OUTPUT_FILE}")

    print(f"\nDa dodaš {len(results)} Hygglo primera u training_data.csv, unesi 'y':")
    answer = input("Dodati? (y/n): ").strip().lower()
    if answer == 'y':
        with open(TRAINING_DATA_FILE, 'a', newline='', encoding='utf-8') as f:
            writer = csv.DictWriter(f, fieldnames=['title', 'category_id', 'source'])
            writer.writerows(results)

        # Final stats
        with open(TRAINING_DATA_FILE, encoding='utf-8') as f:
            total = sum(1 for _ in f) - 1
        print(f"\ntraining_data.csv sada ima {total} redova.")
        print("Pokreni: python train_model.py")


if __name__ == '__main__':
    main()
