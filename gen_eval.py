import subprocess, json, csv

DB_PASS = "Izdajica112!"

def sql(query):
    r = subprocess.run(
        ["docker", "exec", "rentrentout-db", "mysql", "-h", "127.0.0.1",
         "-uroot", f"-p{DB_PASS}", "--skip-column-names",
         "--default-character-set=utf8mb4", "-e", query, "rent_rent_out"],
        capture_output=True)
    return r.stdout.decode("utf-8", errors="replace").strip()

cats = {}
for line in sql("SELECT id, name FROM category;").splitlines():
    parts = line.split("\t")
    if len(parts) == 2:
        cats[parts[0].strip()] = parts[1].strip()

rows = []
for line in sql("SELECT query_text, suggestions_json FROM category_suggestion_log ORDER BY created_at DESC;").splitlines():
    parts = line.split("\t", 1)
    if len(parts) == 2:
        rows.append((parts[0].strip(), parts[1].strip()))

with open("/opt/app/eval_human.csv", "w", newline="", encoding="utf-8") as f:
    w = csv.writer(f)
    w.writerow(["naslov", "top1_kategorija", "top1_%", "top2_kategorija", "top2_%", "top3_kategorija", "top3_%", "tacno_top1", "tacno_top3"])
    for title, sj in rows:
        try:
            sugs = json.loads(sj)
        except:
            continue
        row = [title]
        for i in range(3):
            if i < len(sugs):
                cid = str(sugs[i]["category_id"])
                row += [cats.get(cid, cid), f"{sugs[i]['confidence']*100:.1f}%"]
            else:
                row += ["", ""]
        row += ["", ""]
        w.writerow(row)

with open("/opt/app/eval_ml.csv", "w", newline="", encoding="utf-8") as f:
    w = csv.writer(f)
    w.writerow(["title", "top1_id", "top1_conf", "top2_id", "top2_conf", "top3_id", "top3_conf", "top4_id", "top4_conf", "top5_id", "top5_conf", "correct_category_id"])
    for title, sj in rows:
        try:
            sugs = json.loads(sj)
        except:
            continue
        row = [title]
        for i in range(5):
            if i < len(sugs):
                row += [sugs[i]["category_id"], f"{sugs[i]['confidence']:.4f}"]
            else:
                row += ["", ""]
        row += [""]
        w.writerow(row)

print(f"Generisano {len(rows)} redova.")
print("-> /opt/app/eval_human.csv")
print("-> /opt/app/eval_ml.csv")
