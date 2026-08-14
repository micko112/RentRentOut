import requests
from bs4 import BeautifulSoup
import pandas as pd
import time
import random
import datetime
import re

# --- PODEŠAVANJA ---
DOMEN = "https://hygglo.com"
POCETNE_STRANICE = [
    "https://hygglo.com/uk", 
    "https://hygglo.com/uk/categories"
]
BASE_API_URL = "https://api.hygglo.com/api/v4/product-listings/search"

HEADERS_HTML = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
    "Accept-Language": "en-GB,en-US;q=0.9,en;q=0.8"
}
HEADERS_API = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
    "Accept": "application/json"
}

# --- FAZA 1: PAUK TRAŽI SVE KATEGORIJE ---
print(f"[{datetime.datetime.now().strftime('%H:%M:%S')}] FAZA 1: Pauk pretražuje sajt za svim ID-jevima kategorija...")

kategorije_za_obilazak = POCETNE_STRANICE.copy()
obidjene_kategorije = set()
svi_id_kategorija = set()

while len(kategorije_za_obilazak) > 0:
    trenutni_url = kategorije_za_obilazak.pop(0)
    
    if trenutni_url in obidjene_kategorije:
        continue
    obidjene_kategorije.add(trenutni_url)
    
    try:
        odgovor = requests.get(trenutni_url, headers=HEADERS_HTML, timeout=15)
        if odgovor.status_code != 200:
            continue
            
        soup = BeautifulSoup(odgovor.text, 'html.parser')
        
        # Tražimo sve linkove
        svi_linkovi = soup.find_all('a', href=True)
        for a in svi_linkovi:
            href = a['href']
            
            # Ako je ovo link ka kategoriji
            if '/uk/category/' in href:
                pun_link = DOMEN + href if href.startswith('/') else href
                
                if pun_link not in obidjene_kategorije and pun_link not in kategorije_za_obilazak:
                    kategorije_za_obilazak.append(pun_link)
                
                # EKSTRAKCIJA ID-ja KATEGORIJE (npr. iz /uk/category/9471-power-station izvlači 9471)
                match = re.search(r'/uk/category/(\d+)', href)
                if match:
                    svi_id_kategorija.add(match.group(1))
                    
    except Exception as e:
        print(f"Greška pri pretrazi linka {trenutni_url}: {e}")
    
    # Ispis napretka
    if len(obidjene_kategorije) % 10 == 0:
        print(f" -> Obišao {len(obidjene_kategorije)} stranica. Pronašao {len(svi_id_kategorija)} unikatnih kategorija...")
        
    time.sleep(random.uniform(0.5, 1.5)) # Brza pauza za pauk

print(f"[{datetime.datetime.now().strftime('%H:%M:%S')}] FAZA 1 ZAVRŠENA! Pronađeno ukupno: {len(svi_id_kategorija)} ID-jeva kategorija.")
print("="*60)

# --- FAZA 2: API PUMPA ---
print(f"[{datetime.datetime.now().strftime('%H:%M:%S')}] FAZA 2: Isisavanje svih oglasa preko API-ja...")

svi_oglasi = []

for index, cat_id in enumerate(list(svi_id_kategorija), 1):
    print(f"\n[{index}/{len(svi_id_kategorija)}] Pokrećem API za kategoriju ID: {cat_id}")
    stranica = 1
    
    while True:
        api_url = f"{BASE_API_URL}?country=GB&categoryIds={cat_id}&keywords=&tag=&pageSize=100&pageIndex={stranica}"
        
        try:
            odgovor = requests.get(api_url, headers=HEADERS_API, timeout=15)
            
            if odgovor.status_code != 200:
                print(f"  -> Server odbio zahtev (Status: {odgovor.status_code}). Prekidam ovu kategoriju.")
                break
                
            podaci = odgovor.json()
            
            # Detektovanje gde je lista oglasa u JSON odgovoru
            lista_oglasa = []
            if isinstance(podaci, dict):
                for kljuc in ['items', 'data', 'results', 'listings', 'productListings']:
                    if kljuc in podaci and isinstance(podaci[kljuc], list):
                        lista_oglasa = podaci[kljuc]
                        break
                if not lista_oglasa:
                    lista_oglasa = next((v for v in podaci.values() if isinstance(v, list)), [])
            elif isinstance(podaci, list):
                lista_oglasa = podaci
                
            if len(lista_oglasa) == 0:
                print(f"  -> Nema više oglasa (Stranica {stranica} je prazna). Završeno.")
                break
                
            print(f"  -> Stranica {stranica}: Preuzeto {len(lista_oglasa)} oglasa.")
            svi_oglasi.extend(lista_oglasa)
            
            stranica += 1
            time.sleep(random.uniform(0.5, 1.2)) # Pauza između API zahteva
            
        except Exception as e:
            print(f"  -> Greška pri API zahtevu: {e}")
            break

print("="*60)
print(f"[{datetime.datetime.now().strftime('%H:%M:%S')}] FAZA 2 ZAVRŠENA! Ukupno preuzeto sirovih oglasa: {len(svi_oglasi)}")

# --- FAZA 3: ČIŠĆENJE I ČUVANJE U EXCEL ---
if svi_oglasi:
    print(f"[{datetime.datetime.now().strftime('%H:%M:%S')}] FAZA 3: Formatiranje i uklanjanje duplikata...")
    
    # Normalizujemo JSON u tabelu
    df = pd.json_normalize(svi_oglasi)
    
    # Pošto neki oglasi pripadaju više kategorija, brišemo duplikate na osnovu jedinstvenog ID-ja oglasa
    if 'id' in df.columns:
        broj_pre = len(df)
        df.drop_duplicates(subset=['id'], inplace=True)
        broj_posle = len(df)
        print(f" -> Obrisano {broj_pre - broj_posle} duplikata. Ostalo čistih unikatnih oglasa: {broj_posle}")
    
    # Sve pretvaramo u tekst da Excel ne bi pravio probleme sa formatiranjem
    df = df.astype(str)
    
    trenutno_vreme = datetime.datetime.now().strftime("%H-%M-%S")
    ime_fajla = f"Hygglo_Ultimativna_Baza_{trenutno_vreme}.xlsx"
    
    try:
        df.to_excel(ime_fajla, index=False)
        print(f"\n🏆 MASIVAN USPEH! Svi podaci su bezbedno sačuvani u fajl: '{ime_fajla}'.")
    except Exception as e:
        print("\nUPOZORENJE: Nije uspelo čuvanje u Excel, pokušavam kao CSV...")
        df.to_csv(f"Hygglo_Ultimativna_Baza_{trenutno_vreme}.csv", index=False)
        print("Sačuvano kao CSV fajl!")
else:
    print("\nNijedan oglas nije preuzet. Proverite vezu sa internetom.")