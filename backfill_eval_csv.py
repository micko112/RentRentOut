"""
Jednokratni backfill: cita sve redove iz category_suggestion_log i upisuje ih
u eval_predictions.csv format unutar ML container-a.

Pokretanje na serveru:
    python3 /opt/app/backfill_eval_csv.py
"""
import subprocess, json, csv, sys

DB_PASS = "Izdajica112!"
OUT_FILE = "/tmp/eval_predictions_backfill.csv"
HEADER = ["timestamp", "naslov",
          "top1_kategorija", "top1_%",
          "top2_kategorija", "top2_%",
          "top3_kategorija", "top3_%",
          "tacno_top1", "tacno_top3"]


def sql(query):
    r = subprocess.run(
        ["docker", "exec", "rentrentout-db", "mysql", "-h", "127.0.0.1",
         "-uroot", f"-p{DB_PASS}", "--skip-column-names",
         "--default-character-set=utf8mb4", "-e", query, "rent_rent_out"],
        capture_output=True)
    return r.stdout.decode("utf-8", errors="replace").strip()


# Ucitaj category_names.json direktno iz ML kontejnera
r = subprocess.run(
    ["docker", "exec", "rentrentout-ml", "cat", "/app/category_names.json"],
    capture_output=True)
if r.returncode != 0:
    print("GRESKA: ne mogu da citam category_names.json iz rentrentout-ml")
    sys.exit(1)
cat_names = {str(k): str(v) for k, v in json.loads(r.stdout.decode("utf-8")).items()}

# Dohvati sve logove (od najstarijeg ka najnovijem)
raw = sql(
    "SELECT created_at, query_text, suggestions_json "
    "FROM category_suggestion_log ORDER BY created_at ASC;"
)

rows = []
for line in raw.splitlines():
    parts = line.split("\t", 2)
    if len(parts) == 3:
        rows.append((parts[0].strip(), parts[1].strip(), parts[2].strip()))

with open(OUT_FILE, "w", newline="", encoding="utf-8-sig") as f:
    w = csv.writer(f)
    w.writerow(HEADER)
    for ts, title, sj in rows:
        try:
            sugs = json.loads(sj)
        except Exception:
            continue
        row = [ts, title]
        for i in range(3):
            if i < len(sugs):
                cid = str(sugs[i]["category_id"])
                row += [cat_names.get(cid, cid), f"{sugs[i]['confidence'] * 100:.1f}%"]
            else:
                row += ["", ""]
        row += ["", ""]
        w.writerow(row)

print(f"Generisano {len(rows)} redova -> {OUT_FILE}")
print()
print("Kopiraj u ML kontejner:")
print(f"  docker cp {OUT_FILE} rentrentout-ml:/app/data/eval_predictions.csv")
