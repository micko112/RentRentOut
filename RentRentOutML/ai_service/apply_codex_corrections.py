"""
Čita codex_corrections.txt i upisuje override_category_id u hygglo_category_map.csv.

Format codex_corrections.txt (jedan red po grešci):
  hygglo_id|correct_category_id|komentar
  8608|1601|Battery Charger For Vehicles -> Punjac baterija
  9056|1020|Cordless drill -> Akumulatorska busliica
"""

import csv
import os

CORRECTIONS_FILE = 'codex_corrections.txt'
MAP_FILE = 'hygglo_category_map.csv'


def load_corrections():
    corrections = {}
    skipped = []
    with open(CORRECTIONS_FILE, encoding='utf-8') as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith('#') or line.startswith('|'):
                continue
            parts = line.split('|')
            if len(parts) < 2:
                skipped.append(line)
                continue
            hygglo_id = parts[0].strip()
            correct_id = parts[1].strip()
            if hygglo_id and correct_id:
                corrections[hygglo_id] = correct_id
    if skipped:
        print(f"Preskočeno (loš format): {len(skipped)} redova")
    return corrections


def main():
    if not os.path.exists(CORRECTIONS_FILE):
        print(f"Nema fajla: {CORRECTIONS_FILE}")
        print("Napravi ga sa Codex korekcijama u formatu: hygglo_id|correct_category_id|komentar")
        return

    corrections = load_corrections()
    print(f"Učitano {len(corrections)} korekcija")

    with open(MAP_FILE, encoding='utf-8-sig') as f:
        reader = csv.DictReader(f)
        id_col = reader.fieldnames[0]
        fieldnames = reader.fieldnames
        rows = list(reader)

    applied = 0
    for row in rows:
        hid = row[id_col]
        if hid in corrections:
            row['override_category_id'] = corrections[hid]
            applied += 1

    with open(MAP_FILE, 'w', newline='', encoding='utf-8-sig') as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)

    print(f"Primenjeno: {applied} korekcija")
    print(f"Nisu pronađeni u mapi: {len(corrections) - applied} ID-eva")
    print(f"\nSledeći korak: python step3_finalize.py")


if __name__ == '__main__':
    main()
