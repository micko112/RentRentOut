"""
Dodaje ciljane srpske primere za kategorije koje model predviđa pogrešno.
Svi primeri idu sa source='real' → biće oversample-ovani 100x u train_model.py.
"""

import csv

TRAINING_DATA = 'training_data.csv'

# (category_id, [titles])
TARGETED = [
    # 1186 — Šrafilice / akumulatorska bušilica (konfuzija sa wrong cat)
    (1186, [
        "akumulatorska bušilica",
        "Makita akumulatorska bušilica",
        "Bosch akumulatorska bušilica",
        "Hilti akumulatorska bušilica",
        "DeWalt akumulatorska bušilica",
        "bušilica bez kabla",
        "bežična bušilica",
        "akumulatorska bušilica 18V",
        "bušilica odvijač akumulatorska",
        "Makita bušilica odvijač",
        "cordless drill bušilica",
        "AEG akumulatorska bušilica",
        "Milwaukee akumulatorska bušilica",
        "Ryobi akumulatorska bušilica",
        "bušilica za drvo akumulatorska",
    ]),

    # 1185 — Rotacioni čekić / pneumatski čekić
    (1185, [
        "pneumatski čekić",
        "rotacioni čekić",
        "sekač betona",
        "sekač za beton",
        "Hilti rotacioni čekić",
        "Bosch rotacioni čekić",
        "Makita rotacioni čekić",
        "čekić za beton",
        "pneumatski čekić za beton",
        "bušilica čekić kombinovana",
        "SDS čekić",
        "SDS plus čekić",
        "SDS max čekić",
        "demolition hammer čekić",
        "čekić za rušenje",
        "električni čekić za beton",
        "čekić za zidove",
        "perforator čekić",
    ]),

    # 1322 — Visokotlačni perač / karcher
    (1322, [
        "karcher",
        "Kärcher",
        "Karcher visokotlačni",
        "visokotlačni perač",
        "mašina za pranje pod pritiskom",
        "perač pod pritiskom",
        "high pressure washer",
        "Kärcher K7",
        "Kärcher K5",
        "Kärcher K4",
        "visoki pritisak pranje",
        "perač automobila pritiskom",
        "mašina za pranje auta",
        "pranje fasade pod pritiskom",
        "perač terasa i dvorišta",
        "elektro perač pod pritiskom",
        "Bosch visokotlačni perač",
    ]),

    # 1195 — Bager / mini bager
    (1195, [
        "mini bager",
        "bager rovokopač",
        "bager za iskop",
        "mali bager",
        "bobcat",
        "Bobcat E17",
        "mini excavator",
        "rovokopač",
        "bager za kanale",
        "Kubota mini bager",
        "Caterpillar mini bager",
        "bager 1.5t",
        "bager 3t",
        "bager 5t",
        "kompaktni bager",
    ]),

    # 1196 — Utovarivač (bobcat, wheel loader)
    (1196, [
        "utovarivač",
        "bobcat utovarivač",
        "Bobcat S550",
        "kompaktni utovarivač",
        "mini utovarivač",
        "skid steer",
        "wheel loader",
        "prednji utovarivač",
        "mašina za utovar",
        "utovarivač na gusenicama",
    ]),

    # 1024 — Agregat / generator struje
    (1024, [
        "generator struje",
        "agregat za struju",
        "električni agregat",
        "agregat 5kW",
        "agregat 10kW",
        "Honda generator",
        "Briggs Stratton agregat",
        "benzinski generator",
        "dizel generator",
        "prenosni generator",
        "inverter generator",
        "generator za gradilište",
        "agregat za događaje",
        "generator struje 220V",
        "strujni agregat",
    ]),

    # 1246 — Projektor (ne slajd projektor)
    (1246, [
        "projektor",
        "projektor za film",
        "projektor za prezentacije",
        "Epson projektor",
        "Benq projektor",
        "Optoma projektor",
        "laser projektor",
        "4K projektor",
        "Full HD projektor",
        "portabl projektor",
        "prenosni projektor",
        "projektor za bioskop",
        "projektor za sala",
        "LED projektor",
        "LCD projektor",
        "projektor 3000 lumena",
        "bim projektor",
        "video projektor",
    ]),

    # 1426 — Ostalo u odeći / smoking, muška odela
    (1426, [
        "smoking",
        "muški smoking",
        "crni smoking",
        "odelo za venčanje",
        "muško svečano odelo",
        "svečano odelo",
        "frak odelo",
        "tuxedo iznajmljivanje",
        "odelo za zabavu",
        "odelo za maturu",
        "odelo za proslavu",
        "muško odelo za svadbu",
        "svečani kaput",
        "leptir mašna komplet",
        "smoking komplet",
    ]),

    # 1273 — Ostali prostori / stan na dan
    (1273, [
        "stan na dan Beograd",
        "stan na dan Novi Sad",
        "apartman za noć",
        "iznajmljivanje stana na dan",
        "soba za iznajmljivanje",
        "soba na dan",
        "apartman centar Beograd",
        "studio apartman iznajmljivanje",
        "stan za dan Niš",
        "stan na dan Zlatibor",
        "smeštaj na dan",
        "privatni smeštaj na dan",
        "apartman za kratki boravak",
    ]),

    # 1297 — Gimbali i stabilizatori
    (1297, [
        "gimbal stabilizator",
        "DJI Ronin gimbal",
        "stabilizator za kameru",
        "gimbal za telefon",
        "gimbal za DSLR",
        "trostepeni gimbal",
        "električni stabilizator kamere",
        "Zhiyun Crane gimbal",
        "Zhiyun Smooth gimbal",
        "Moza gimbal",
        "FeiyuTech gimbal",
        "gimbal za video",
        "stabilizator slike",
        "handheld gimbal",
        "gimbal 3-osni",
    ]),

    # 1161 — Mobilni telefon
    (1161, [
        "iphone",
        "samsung telefon",
        "iPhone iznajmljivanje",
        "Android telefon",
        "pametni telefon",
        "iPhone 14",
        "iPhone 15",
        "Samsung Galaxy",
        "mobilni telefon za iznajmljivanje",
        "privremeni telefon",
        "rezervni telefon",
        "telefon za putovanje",
        "SIM free telefon",
        "smartfon",
    ]),

    # 1117 — Viljuškar
    (1117, [
        "viljuškar",
        "električni viljuškar",
        "dizel viljuškar",
        "Toyota viljuškar",
        "Still viljuškar",
        "Linde viljuškar",
        "viljuškar 2.5t",
        "viljuškar 3t",
        "viljuškar 5t",
        "counterbalance viljuškar",
        "viljuškar za magacin",
        "viljuškar za gradilište",
        "forklift",
        "viljuškar reach truck",
        "viljuškar paletni",
    ]),

    # 1232 — Građevinska skela
    (1232, [
        "skela",
        "skela za fasadu",
        "građevinska skela",
        "metalna skela",
        "fasadna skela",
        "skela za bojenje",
        "aluminijumska skela",
        "skela 6m",
        "skela 10m",
        "pokretna skela",
        "skela za unutrašnje radove",
        "skela za renoviranje",
        "skela komplet",
        "scaffolding skela",
        "radna platforma skela",
    ]),

    # 105 — Ventilacija i grejanje / klima uređaj
    (105, [
        "klima uređaj",
        "prenosna klima",
        "mobilna klima",
        "klima uređaj za iznajmljivanje",
        "rashladni uređaj",
        "ventilator za hlađenje",
        "industrijski ventilator",
        "rashladni uređaj za dogadjaje",
        "klima za šator",
        "evaporativni hladnjak",
        "prenosni hladnjak",
    ]),

    # 1209 — Beton i armatura / vibrator, pumpa za beton
    (1209, [
        "vibrator za beton",
        "pumpa za beton",
        "vibratorska igla za beton",
        "električni vibrator betona",
        "Wacker vibrator betona",
        "mešalica za beton",
        "mešalica 200l",
        "mešalica 300l",
        "betonska pumpa",
        "crevo za beton",
        "armatura za beton",
        "bušilica za beton",
        "sekač armature",
        "savijač armature",
        "betoner pumpa",
    ]),

    # 1032 — Ubodna testera (jigsaw)
    (1032, [
        "ubodna testera",
        "ubodna pila",
        "Bosch ubodna testera",
        "Makita ubodna testera",
        "DeWalt ubodna testera",
        "jigsaw testera",
        "ubodna testera za drvo",
        "ubodna testera za metal",
        "električna ubodna testera",
        "akumulatorska ubodna testera",
        "ubodna testera 650W",
        "vibraciona testera",
        "testera za krive rezove",
        "ubodna pila za drvo",
        "Festool ubodna testera",
    ]),

    # 1042 — Sabljasta testera (reciprocating saw)
    (1042, [
        "sabljasta testera",
        "sabljasta pila",
        "Bosch sabljasta testera",
        "Makita sabljasta testera",
        "Milwaukee sabljasta testera",
        "DeWalt sabljasta testera",
        "električna sabljasta testera",
        "akumulatorska sabljasta testera",
        "sabljasta testera za drvo",
        "sabljasta testera za beton",
        "sabljasta testera za rušenje",
        "tigertail testera",
        "alligator testera",
        "sabljasta pila za grane",
        "reciprocating saw",
    ]),

    # 1309 — Motorna testera (chainsaw)
    (1309, [
        "motorna testera",
        "motorna testera Stihl",
        "Husqvarna motorna testera",
        "Stihl MS 261",
        "Husqvarna 450",
        "motorna testera za drva",
        "benzinska motorna testera",
        "električna motorna testera",
        "lančana testera",
        "Stihl chainsaw",
        "Husqvarna chainsaw",
        "motorna pila za drvo",
        "testera za seču drva",
        "motorna testera 40cm",
        "motorna testera 50cm",
    ]),

    # 1568 — Skijanje i snowboard
    (1568, [
        "skije",
        "snoubord",
        "snowboard",
        "ski oprema",
        "carving skije",
        "all mountain skije",
        "powder skije",
        "Atomic skije",
        "Rossignol skije",
        "Head skije",
        "Salomon skije",
        "skije za odrasle",
        "skije za decu",
        "snowboard daska",
        "snowboard set",
        "Burton snowboard",
        "snoubord za početnike",
        "ski set komplet",
        "skije i štapovi",
    ]),

    # 1634 — Ostalo u prikolicama (prikolica — generička)
    (1634, [
        "prikolica",
        "auto prikolica",
        "prikolica za auto",
        "mala prikolica",
        "prikolica 750kg",
        "prikolica 1000kg",
        "laka prikolica",
        "prikolica za prevoz",
        "jednoosovinska prikolica",
        "prikolica za selidbu",
        "prikolica za teret",
        "prikolica sa ceradom",
        "standardna prikolica",
        "prikolica za iznajmljivanje",
        "prikolica rent",
    ]),

    # 1329 — Kombinovane merdevine
    (1329, [
        "merdevine aluminijumske",
        "aluminijumske merdevine",
        "kombinovane merdevine",
        "multifunkcijske merdevine",
        "merdevine 3u1",
        "merdevine za fasadu",
        "merdevine za krov",
        "merdevine 4m",
        "merdevine 6m",
        "sklopive merdevine",
        "teleskopske merdevine",
        "Hailo merdevine",
        "Werner merdevine",
        "merdevine za bojenje",
        "merdevine za renoviranje",
    ]),

    # 1426 — Ostalo u odeći — venčanica, svečane haljine
    # (dodati na postojeće smoking primere)
    (1426, [
        "venčanica",
        "venčanica iznajmljivanje",
        "svečana venčanica",
        "princeza venčanica",
        "A-line venčanica",
        "haljina za matursku",
        "svečana haljina",
        "haljina za proslavu",
        "maturska haljina",
        "koktel haljina iznajmljivanje",
        "večernja haljina",
        "haljina za venčanje",
        "bridesmaid haljina",
        "haljina za goste na svadbi",
    ]),
]


def main():
    with open(TRAINING_DATA, encoding='utf-8', errors='replace') as f:
        existing = list(csv.DictReader(f))

    new_rows = []
    for cat_id, titles in TARGETED:
        for title in titles:
            new_rows.append({
                'title': title,
                'category_id': str(cat_id),
                'source': 'real',
            })

    all_rows = existing + new_rows

    with open(TRAINING_DATA, 'w', newline='', encoding='utf-8') as f:
        w = csv.DictWriter(f, fieldnames=['title', 'category_id', 'source'])
        w.writeheader()
        w.writerows(all_rows)

    by_source = {}
    for r in all_rows:
        by_source[r['source']] = by_source.get(r['source'], 0) + 1

    print(f"Dodato {len(new_rows)} novih primera (real).")
    print(f"training_data.csv ukupno: {len(all_rows)} redova")
    for src, cnt in sorted(by_source.items()):
        print(f"  {src}: {cnt}")


if __name__ == '__main__':
    main()
