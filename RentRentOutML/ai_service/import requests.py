import requests
from bs4 import BeautifulSoup
import pandas as pd
import time
import random

# --- KONFIGURACIJA ---
# TODO: Zamenite ovaj URL sa stvarnim linkom pretrage (bez broja stranice na kraju)
BASE_URL = "https://iznajmiunajmi.rs/" 

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
}

svi_oglasi = []
UKUPNO_STRANICA = 35 

print("Započinjem prikupljanje podataka...")

for stranica in range(1, UKUPNO_STRANICA + 1):
    # Podešavanje linka za trenutnu stranicu (prilagodite parametar ako nije ?page=)
    url_stranice = f"{BASE_URL}?page={stranica}"
    
    print(f"Učitavam stranicu {stranica} od {UKUPNO_STRANICA}... ({url_stranice})")
    
    try:
        odgovor = requests.get(url_stranice, headers=HEADERS, timeout=10)
        
        if odgovor.status_code != 200:
            print(f"Greška pri učitavanju stranice {stranica}. Status kod: {odgovor.status_code}")
            continue
            
        soup = BeautifulSoup(odgovor.text, 'html.parser')
        
        # Pronalazimo sve kartice na stranici na osnovu Vašeg HTML-a
        kartice_oglasa = soup.find_all('div', class_='listivo-listing-card-v4__inner')
        
        if not kartice_oglasa:
            print(f"Upozorenje: Na stranici {stranica} nisu pronađeni oglasi. Proverite strukturu.")
            continue
            
        for kartica in kartice_oglasa:
            try:
                # 1. Naslov
                naslov_el = kartica.find('h3', class_='listivo-listing-card-v4__name')
                naslov = naslov_el.text.strip() if naslov_el else "Nema naslova"
                
                # 2. Cena
                cena_el = kartica.find('div', class_='listivo-listing-card-v4__value')
                cena = cena_el.text.strip() if cena_el else "Nema cene"
                
                # 3. Adresa
                adresa_el = kartica.find('span', class_='listivo-listing-card-v4__address-text')
                adresa = adresa_el.text.strip() if adresa_el else "Nema adrese"
                
                # 4. Broj pregleda
                pregledi_el = kartica.find('div', class_='listivo-listing-card-v4__views')
                pregledi = "0"
                if pregledi_el:
                    # Izvlačimo samo broj unutar <span> taga ako postoji
                    span_pregledi = pregledi_el.find('span')
                    pregledi = span_pregledi.text.strip() if span_pregledi else pregledi_el.text.strip()
                
                # 5. Link do oglasa
                # Obično se unutar kartice nalazi link (<a> tag). Pokušavamo da ga pronađemo.
                link_el = kartica.find('a')
                link = "Nema linka"
                if link_el and 'href' in link_el.attrs:
                    link = link_el['href']
                    # Ako je link relativan (npr. /oglas/123), spajamo ga sa domenom
                    # TODO: Zamenite sa stvarnim domenom sajta
                    if link.startswith('/'):
                        link = "https://www.primer-sajta.com" + link

                # Dodavanje u listu
                svi_oglasi.append({
                    "Naslov": naslov,
                    "Cena": cena,
                    "Adresa": adresa,
                    "Pregledi": pregledi,
                    "Link": link
                })
                
            except Exception as e:
                print(f"Greška pri obradi oglasa: {e}")
                continue
                
    except Exception as e:
        print(f"Došlo je do greške na stranici {stranica}: {e}")
        
    # Pauza između učitavanja stranica (1.5 do 3 sekunde) da izbegnete blokadu
    time.sleep(random.uniform(1.5, 3.0))

# --- ČUVANJE U EXCEL ---
if svi_oglasi:
    df = pd.DataFrame(svi_oglasi)
    ime_fajla = "oglasi_podaci.xlsx"
    df.to_excel(ime_fajla, index=False)
    print(f"\nUspešno sačuvano {len(svi_oglasi)} oglasa u fajl '{ime_fajla}'.")
else:
    print("\nNije pronađen nijedan oglas. Proverite podešavanja.")