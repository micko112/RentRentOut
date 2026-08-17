"""Drugi krug ciljanih primera — kategorije koje su još uvek pogrešne."""
import csv

TRAINING_DATA = 'training_data.csv'

TARGETED = [
    # 1055 — Orbitalna brusilica (vibraciona brusilica)
    (1055, [
        "vibraciona brusilica",
        "orbitalna brusilica",
        "delta brusilica",
        "trokutna brusilica",
        "Bosch orbitalna brusilica",
        "Makita orbitalna brusilica",
        "DeWalt orbitalna brusilica",
        "random orbit sander",
        "ekscentrična brusilica",
        "brusilica za fino brušenje",
        "brusilica za lakiranje",
        "akumulatorska orbitalna brusilica",
        "brusilica za drvo orbitalna",
        "multitool brusilica",
        "vibraciona brusilica 125mm",
    ]),

    # 1078 — Libela (nivo, spirit level)
    (1078, [
        "libela",
        "vodena libela",
        "aluminijumska libela",
        "libela 60cm",
        "libela 120cm",
        "libela 180cm",
        "magnetna libela",
        "digitalna libela",
        "građevinska libela",
        "libela za zidare",
        "nivo libela",
        "Stanley libela",
        "Stabila libela",
        "libela za pločice",
        "precizna libela",
    ]),

    # 1224 — Električni aparat za zavarivanje (inverter za zavarivanje)
    (1224, [
        "inverter za zavarivanje",
        "MIG MAG aparat",
        "TIG aparat za zavarivanje",
        "MMA inverter",
        "Lincoln Electric aparat",
        "Esab aparat za zavarivanje",
        "Fronius aparat",
        "zavarivač inverter 200A",
        "zavarivač MIG 180A",
        "električni zavarivač",
        "inverter zavarivač 150A",
        "CO2 aparat za zavarivanje",
        "poluautomatski aparat za zavarivanje",
        "welder inverter",
        "elektroaparat za zavarivanje",
    ]),

    # 1569 — Sanke
    (1569, [
        "saonice",
        "dečije saonice",
        "plastične saonice",
        "drvene saonice",
        "saonice za sneg",
        "bob saonice",
        "saonice za planinu",
        "saonice za skijanje",
        "saonice za decu",
        "zimske saonice",
        "tobogan saonice",
        "sportske saonice",
        "saonice iznajmljivanje",
        "saonice za klizanje niz breg",
        "klasične drvene saonice",
    ]),

    # 1157 — VR naočale
    (1157, [
        "VR naočare",
        "VR naočale",
        "virtuelna realnost naočare",
        "Meta Quest",
        "Meta Quest 3",
        "Oculus Quest",
        "PlayStation VR",
        "PSVR2",
        "HTC Vive",
        "naočare za virtuelnu realnost",
        "VR headset",
        "VR glasses",
        "mixed reality naočare",
        "VR set za iznajmljivanje",
        "virtuelna realnost headset",
    ]),

    # 1619 — Kanu
    (1619, [
        "kanu",
        "kanadski kanu",
        "kanu čamac",
        "drveni kanu",
        "plastični kanu",
        "kanu za reku",
        "kanu za jezero",
        "kanu za dvoje",
        "kanu iznajmljivanje",
        "Old Town kanu",
        "Royalex kanu",
        "kanu veslo",
        "kanu set",
        "porodični kanu",
        "turski kanu",
    ]),

    # 1540 — Čamac na gumu
    (1540, [
        "gumeni čamac",
        "gumeni čun",
        "naduvajući čamac",
        "čamac na naduvavanje",
        "Intex čamac",
        "Zodiac čamac",
        "raft čamac",
        "gumeni rafting čamac",
        "dinghy čamac",
        "inflatable boat",
        "gumenjak",
        "čamac za reku",
        "mali gumeni čamac",
        "čamac za pecanje gumeni",
        "naduvani čamac za decu",
    ]),

    # 1341 — Kosilica (ne traktorska)
    (1341, [
        "kosilica za travu",
        "motorna kosilica",
        "benzinska kosilica",
        "električna kosilica",
        "kosilica bez kabla",
        "push kosilica",
        "kosilica 46cm",
        "kosilica 51cm",
        "Honda kosilica",
        "Bosch kosilica",
        "Husqvarna push kosilica",
        "ručna kosilica",
        "kosilica za mali travnjak",
        "mulching kosilica",
        "kosilica sa košem",
    ]),

    # 1311 — Pumpa za odvodnjavanje
    (1311, [
        "potapajuća pumpa",
        "pumpa za vodu",
        "drenažna pumpa",
        "pumpa za podrum",
        "električna pumpa za vodu",
        "Grundfos pumpa",
        "submerzna pumpa",
        "pumpa za odvodnjavanje",
        "pumpa za bazen",
        "pumpa za bunar",
        "pumpa za septičku jamu",
        "potopna pumpa",
        "pumpa 1000W",
        "pumpa za izlivenu vodu",
        "pumpa za gradilište",
    ]),

    # 1289 — Studijska lampa / LED panel (foto/video osvetljenje)
    (1289, [
        "studijsko osvetljenje",
        "LED panel osvetljenje",
        "foto osvetljenje",
        "video osvetljenje",
        "LED panel za snimanje",
        "Godox LED panel",
        "Aputure LED panel",
        "studijska rasveta",
        "LED video svetlo",
        "bi-color LED panel",
        "RGB LED panel",
        "LED svetlo za studio",
        "panel svetlo za fotografiju",
        "studijsko LED svetlo",
        "foto LED svetlo",
    ]),

    # 808 — Ostala vozila (električni romobil, skuter, motor)
    (808, [
        "električni romobil",
        "trotinet za iznajmljivanje",
        "električni trotinet",
        "električni skuter",
        "e-romobil",
        "električni skuterid",
        "Segway",
        "hoverboard",
        "jednokolica električna",
        "self-balancing skuter",
    ]),

    # 1639 — Ostalo u motociklima (motor za iznajmljivanje)
    (1639, [
        "motor za iznajmljivanje",
        "motocikl iznajmljivanje",
        "motor na dan",
        "iznajmljivanje motocikla",
        "motocikl rent",
        "Honda motocikl",
        "Yamaha motocikl",
        "Kawasaki motocikl",
        "motor 125cc",
        "motor 250cc",
        "motor 500cc",
        "motocikl za ture",
        "enduro motocikl",
        "naked motocikl",
        "motocikl za vožnju",
    ]),

    # 1568 — Skijanje i snowboard (ski čizme, ski boots)
    (1568, [
        "ski čizme",
        "ski pancerice",
        "ski boots",
        "ski čizme Rossignol",
        "ski čizme Atomic",
        "ski čizme Salomon",
        "ski čizme Head",
        "ski čizme za odrasle",
        "ski čizme za decu",
        "ski čizme 26.5",
        "ski pancerice za početnike",
        "carving pancerice",
        "ski čizme muške",
        "ski čizme ženske",
        "ski bindings",
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

    print(f"Dodato {len(new_rows)} novih primera.")
    print(f"training_data.csv ukupno: {len(all_rows)} redova")
    for src, cnt in sorted(by_source.items()):
        print(f"  {src}: {cnt}")


if __name__ == '__main__':
    main()
