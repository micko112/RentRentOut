"""
Korak 1: Mapira 881 Hygglo kategorija (EN) na naših 644 leaf kategorija (SR)
koristeći semantičku sličnost multilingual encodera.

Output: hygglo_category_map.csv
  - Preglej fajl, ispravi greške u koloni 'override_category_id'
  - Ostavi 'override_category_id' prazno gde je automatski match tačan
  - Onda pokreni step2_translate.py
"""

import csv
import numpy as np
from sentence_transformers import SentenceTransformer

SOURCE_CSV = 'Hygglo_Ultimativna_Baza_18-16-12.csv'
TAXONOMY_CSV = 'taxonomy.csv'
OUTPUT_MAP = 'hygglo_category_map.csv'


def load_our_leaf_categories():
    cats = {}
    with open(TAXONOMY_CSV, encoding='utf-8') as f:
        for row in csv.DictReader(f):
            if row['is_leaf'] == '1':
                parent = row['parent_name']
                name = row['name']
                # Full context za bolji semantic match: "Roditeljska kategorija > Ime"
                full_label = f"{parent} > {name}" if parent else name
                cats[row['id']] = {
                    'name': name,
                    'parent': parent,
                    'full_label': full_label,
                }
    return cats


def load_hygglo_categories():
    cats = {}
    with open(SOURCE_CSV, encoding='utf-8') as f:
        for row in csv.DictReader(f):
            cid = row['product.category.id']
            if cid not in cats:
                cats[cid] = row['product.category.name']
    return cats


def main():
    print("Učitavanje kategorija...")
    our_cats = load_our_leaf_categories()
    hygglo_cats = load_hygglo_categories()
    print(f"  Naše leaf kategorije: {len(our_cats)}")
    print(f"  Hygglo kategorije: {len(hygglo_cats)}")

    print("\nUčitavanje encodera (paraphrase-multilingual-mpnet-base-v2)...")
    model = SentenceTransformer('paraphrase-multilingual-mpnet-base-v2')

    our_ids = list(our_cats.keys())
    our_labels = [our_cats[i]['full_label'] for i in our_ids]

    hygglo_ids = list(hygglo_cats.keys())
    hygglo_labels = [hygglo_cats[i] for i in hygglo_ids]

    print("\nEmbedding naših kategorija (SR + EN parent context)...")
    our_emb = model.encode(our_labels, show_progress_bar=True, batch_size=64, normalize_embeddings=True)

    print("\nEmbedding Hygglo kategorija (EN)...")
    hygglo_emb = model.encode(hygglo_labels, show_progress_bar=True, batch_size=64, normalize_embeddings=True)

    # Cosine similarity: (881 x 644) — dot product radi jer su vektori normalizovani
    print("\nRačunanje sličnosti...")
    sim_matrix = hygglo_emb @ our_emb.T  # shape (881, 644)

    results = []
    for i, hid in enumerate(hygglo_ids):
        sims = sim_matrix[i]
        top3 = np.argsort(sims)[-3:][::-1]

        results.append({
            'hygglo_id': hid,
            'hygglo_name': hygglo_cats[hid],
            'matched_category_id': our_ids[top3[0]],
            'matched_category_name': our_cats[our_ids[top3[0]]]['name'],
            'matched_parent': our_cats[our_ids[top3[0]]]['parent'],
            'similarity': round(float(sims[top3[0]]), 4),
            'alt1_id': our_ids[top3[1]],
            'alt1_name': our_cats[our_ids[top3[1]]]['name'],
            'alt1_sim': round(float(sims[top3[1]]), 4),
            'alt2_id': our_ids[top3[2]],
            'alt2_name': our_cats[our_ids[top3[2]]]['name'],
            'alt2_sim': round(float(sims[top3[2]]), 4),
            # Popuni ovo ako automatski match nije tačan:
            'override_category_id': '',
        })

    results.sort(key=lambda x: x['hygglo_name'])

    fieldnames = [
        'hygglo_id', 'hygglo_name',
        'matched_category_id', 'matched_category_name', 'matched_parent', 'similarity',
        'alt1_id', 'alt1_name', 'alt1_sim',
        'alt2_id', 'alt2_name', 'alt2_sim',
        'override_category_id',
    ]
    with open(OUTPUT_MAP, 'w', newline='', encoding='utf-8-sig') as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(results)

    high = sum(1 for r in results if r['similarity'] >= 0.50)
    medium = sum(1 for r in results if 0.35 <= r['similarity'] < 0.50)
    low = sum(1 for r in results if r['similarity'] < 0.35)

    print(f"\n=== Rezultat mapiranja ===")
    print(f"  Visoka sličnost (>=0.50): {high}  — verovatno tačno")
    print(f"  Srednja   (0.35–0.50): {medium}  — preporuča se pregled")
    print(f"  Niska     (<0.35):      {low}  — obavezno proveri ili preskoči")
    print(f"\nSačuvano: {OUTPUT_MAP}")
    print("\nSledeci koraci:")
    print("  1. Otvori hygglo_category_map.csv u Excelu/Calc")
    print("  2. Sortiraj po 'similarity' rastuce — pregledaj niske")
    print("  3. Gde match nije dobar, upiši tacni category_id u 'override_category_id'")
    print("  4. Pokreni: python step2_translate.py")


if __name__ == '__main__':
    main()
