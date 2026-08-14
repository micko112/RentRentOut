"""
Korak 2: Prevodi 87k jedinstvenih Hygglo naslova EN → SR
koristeći Helsinki-NLP offline model (besplatno, ~2-4h CPU).

Features:
  - Deduplikacija po product.id (87k, ne 160k)
  - Checkpoint svakih 500 batch-eva — bezbedno prekinuti i nastaviti
  - GPU automatski ako postoji, inace CPU
  - Batch processing (32 naslova odjednom)

Output: hygglo_translated_checkpoint.csv
"""

import csv
import os
import torch
from transformers import MarianMTModel, MarianTokenizer
from tqdm import tqdm

SOURCE_CSV = 'Hygglo_Ultimativna_Baza_18-16-12.csv'
CHECKPOINT_FILE = 'hygglo_translated_checkpoint.csv'
MODEL_NAME = 'Helsinki-NLP/opus-mt-tc-base-en-sh'  # English → Serbo-Croatian
BATCH_SIZE = 32
SAVE_EVERY = 500  # batch-eva (16k naslova)


def load_unique_products():
    products = {}
    with open(SOURCE_CSV, encoding='utf-8') as f:
        for row in csv.DictReader(f):
            pid = row['product.id']
            if pid not in products:
                products[pid] = {
                    'product_id': pid,
                    'title_en': row['product.name'],
                    'hygglo_category_id': row['product.category.id'],
                }
    return list(products.values())


def load_done_ids():
    if not os.path.exists(CHECKPOINT_FILE):
        return set()
    done = set()
    with open(CHECKPOINT_FILE, encoding='utf-8') as f:
        for row in csv.DictReader(f):
            done.add(row['product_id'])
    print(f"Checkpoint: {len(done)} već prevedenih naslova.")
    return done


def translate_batch(model, tokenizer, texts, device):
    inputs = tokenizer(
        texts,
        return_tensors='pt',
        padding=True,
        truncation=True,
        max_length=128,
    ).to(device)
    with torch.no_grad():
        outputs = model.generate(**inputs, num_beams=2, max_new_tokens=128)
    return [tokenizer.decode(o, skip_special_tokens=True) for o in outputs]


def main():
    print("Učitavanje jedinstvenih proizvoda...")
    all_products = load_unique_products()
    print(f"Jedinstvenih naslova: {len(all_products)}")

    done_ids = load_done_ids()
    remaining = [p for p in all_products if p['product_id'] not in done_ids]
    print(f"Preostalo za prevod: {len(remaining)}")

    if not remaining:
        print("Svi naslovi su već prevedeni. Pokreni step3_finalize.py")
        return

    device = 'cuda' if torch.cuda.is_available() else 'cpu'
    print(f"Uređaj: {device.upper()}")
    if device == 'cpu':
        est_min = len(remaining) / 32 / 8  # ~8 batch/sec na CPU
        print(f"Procena vremena na CPU: ~{est_min:.0f} minuta")

    print(f"\nPreuzimanje modela {MODEL_NAME}...")
    tokenizer = MarianTokenizer.from_pretrained(MODEL_NAME)
    model = MarianMTModel.from_pretrained(MODEL_NAME).to(device)
    model.eval()

    # Append mod — nastavlja od checkpointa
    first_write = not os.path.exists(CHECKPOINT_FILE)
    fieldnames = ['product_id', 'title_en', 'title_sr', 'hygglo_category_id']

    buffer = []
    batches = [remaining[i:i + BATCH_SIZE] for i in range(0, len(remaining), BATCH_SIZE)]

    def flush_buffer(buf, append):
        mode = 'a' if append else 'w'
        with open(CHECKPOINT_FILE, mode, newline='', encoding='utf-8') as f:
            writer = csv.DictWriter(f, fieldnames=fieldnames)
            if not append:
                writer.writeheader()
            writer.writerows(buf)

    try:
        for batch_idx, batch in enumerate(tqdm(batches, desc="Prevod EN→SR")):
            titles = [p['title_en'] for p in batch]
            translations = translate_batch(model, tokenizer, titles, device)

            for product, sr in zip(batch, translations):
                buffer.append({
                    'product_id': product['product_id'],
                    'title_en': product['title_en'],
                    'title_sr': sr.strip(),
                    'hygglo_category_id': product['hygglo_category_id'],
                })

            # Sačuvaj checkpoint periodično
            if len(buffer) >= SAVE_EVERY * BATCH_SIZE or batch_idx == len(batches) - 1:
                flush_buffer(buffer, append=not first_write)
                first_write = False
                buffer = []

    except KeyboardInterrupt:
        if buffer:
            flush_buffer(buffer, append=not first_write)
        print(f"\nPrekinuto. Checkpoint sačuvan — nastavi sa: python step2_translate.py")
        return

    print(f"\nPrevod završen! Sačuvano u: {CHECKPOINT_FILE}")
    print("Sledeći korak: python step3_finalize.py")


if __name__ == '__main__':
    main()
