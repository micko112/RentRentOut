"""
Brise sve sinteticke primere iz training_data.csv.
Ostavlja samo source='real'.
Pravi backup pre brisanja.
"""

import csv
import shutil
import os

TRAINING_FILE = 'training_data.csv'
BACKUP_FILE = 'training_data_backup_with_synthetic.csv'


def main():
    shutil.copy(TRAINING_FILE, BACKUP_FILE)
    print(f"Backup sacuvan: {BACKUP_FILE}")

    with open(TRAINING_FILE, encoding='utf-8') as f:
        rows = list(csv.DictReader(f))

    before = len(rows)
    real_rows = [r for r in rows if r['source'] == 'real']
    removed = before - len(real_rows)

    with open(TRAINING_FILE, 'w', newline='', encoding='utf-8') as f:
        writer = csv.DictWriter(f, fieldnames=['title', 'category_id', 'source'])
        writer.writeheader()
        writer.writerows(real_rows)

    print(f"Pre:    {before} redova")
    print(f"Posle:  {len(real_rows)} redova (samo real)")
    print(f"Obrisano: {removed} sintetičkih")


if __name__ == '__main__':
    main()
