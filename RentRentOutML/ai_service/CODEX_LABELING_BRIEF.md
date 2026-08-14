# Codex Labeling Task — Category Assignment za realne oglase

## Kontekst

RentRentOut je rental marketplace za srpsko tržište. Backend ima taxonomy sa 644 leaf kategorija (npr. "Visokotlačni perač", "DSLR fotoaparat", "Kamp prikolica"). Treniramo neuronsku mrežu koja preporučuje kategoriju kada korisnik ukuca naslov oglasa.

Skinuli smo **333 realnih naslova oglasa sa tri srpska sajta za iznajmljivanje** (dajnadan.rs, swappko.com, drugi). **Nemamo labele** — tvoj zadatak je da svakom naslovu dodeliš `category_id` iz taxonomy-ja.

## Ulazi

Sve u `RentRentOutML/ai_service/`:

- **`titles_to_label.csv`** — kolone: `title`, `source`. 333 redova.
- **`taxonomy.csv`** — kolone: `id`, `name`, `parent_id`, `parent_name`, `is_leaf`. 718 redova ukupno, 644 leaf. **Labeliraj SAMO leaf kategorijama** (`is_leaf == 1`).

## Izlaz

Napravi **`labeled_ads.csv`** sa kolonama:

```
title,category_id,confidence,notes
```

- `title` — kopiraj iz ulaza (identičan string)
- `category_id` — integer iz `taxonomy.csv`, obavezno `is_leaf == 1`
- `confidence` — jedno od `high`, `medium`, `low`
- `notes` — kratka beleška ako je nešto sporno (opciono, prazno ako je jasno)

## Pravila labeliranja

**1. Uvek biraj najspecifičniju leaf kategoriju.**
- "Bosch bušilica" → `1181` (Udarna bušilica) ili `1186` (Šrafilice), ne `101` (parent "Bušilice i šrafilice") — parent nije leaf.
- Ako je više leaf-ova podjednako dobro, uzmi najuži prema opisu; `confidence: medium`.

**2. Iznajmljivanje-specifične reči ignoriši.**
Prefiksi kao "Iznajmljujem", "Najam/Iznajmljivanje", "Rentiranje", "izdavanje", "na dan", brendovi/lokacije — nisu kategorija, samo dodatak. Ekstrahuj proizvod iz naslova.
- Primer: "Najam/Iznajmljivanje Motorna testera za grane" → `1309` (Motorna testera).

**3. Prepoznaj brendove.**
Domenski uobičajeni brendovi mapiraju na kategoriju:
- Hilti, Makita, Bosch, DeWalt, Wurth, Einhell → alati (traži konkretnu podkategoriju u naslovu)
- Karcher / Kärcher → `1322` (Visokotlačni perač)
- DJI (Mavic, Phantom, Avata) → dronovi
- Canon, Nikon, Sony (u foto kontekstu) → fotoaparati
- Sony PS5/PS4/Xbox/Nintendo → `1154` (Konzola)
- Bocmann, Thule → auto oprema / prikolice

**4. Srpski specifičnosti — normalizuj u glavi.**
- "č/ć/š/ž/đ" su često pisani bez kvačica ("busilica", "sirokougaoni")
- "dž" ↔ "dz"
- "hladnjača" (mobilna) → `1492` (Rashladna kutija i torba) ili `1441` (Posuđe i pribor za jelo)? — proveri kontekst; ako je za event/ketering → `1441`, ako za outdoor → `1492`.
- "pagoda" (šator za event) → `1459` (Šator 24-32 m²) ili odgovarajuća veličina; `medium`.

**5. Non-taxonomy oglasi → SKIP.**
Ako oglas jasno nije pokriven taxonomy-jem, u `category_id` upiši string **`SKIP`** i objasni u `notes`. Primeri koji verovatno idu na SKIP:
- Usluge (prevoz, transport, dubinsko pranje) — ovo je servis, ne rental predmeta. `notes: usluga, ne predmet`
- Nekretnine za dugoročan zakup (garsonjera "350 EUR/mes") — platforma je za kratkoročni najam stvari; ali `901`→`1268-1273` pokrivaju "Prostori". Za garsonjeru/stan bez pandana → `SKIP`; za "iznajmljivanje kancelarije" → `1269`.
- Zdravstveni aparati (BIOPTRON) — nema u taxonomy → `SKIP`.
- Automobili za dugoročan najam bez preciznije podkategorije → `SKIP` (taxonomy nema "Auto" leaf, samo auto **oprema**).

**6. Confidence:**
- `high` — potpuno jasan match iz naslova, ni na šta drugo ne liči
- `medium` — jasna kategorija ali između 2 leaf-a, izabrao/la si po najboljem sudu
- `low` — nagađaš (npr. naslov nejasan; ipak si probao/la)

**7. Nemoj izmišljati ID-jeve.** Svaki `category_id` mora postojati u `taxonomy.csv` sa `is_leaf == 1`.

## Preporučeni workflow

1. Učitaj `taxonomy.csv`, izgradi lookup po parent grupama za brzu navigaciju
2. Za svaki naslov: identifikuj glavnu imenicu → pretraži leaf kategorije čije ime sadrži tu reč (ili je semantički bliska)
3. Ako je više kandidata, oceni po parent group-i koja se najbolje uklapa
4. Zapiši red u `labeled_ads.csv`

## Kvalitet gate

Pre nego što vratiš — proveri:
- [ ] Svih 333 redova pokriveno (ili labeled ili SKIP)
- [ ] Nema duplikata title-ova u izlazu
- [ ] Svi `category_id` osim `SKIP` su validni leaf ID-jevi
- [ ] Ne više od 30% `SKIP`. Ako je više, proveri jesi li propuštao/la valide.

## Deliverable

Sam fajl `labeled_ads.csv`, UTF-8, komma-separated, header prvi red. Ništa drugo.
