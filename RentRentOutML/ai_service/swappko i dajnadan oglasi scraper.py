import requests
from bs4 import BeautifulSoup
import pandas as pd
import time
import random

# --- KONFIGURACIJA - IZABERITE SAJT ---
# Upišite 1 za Dajnadan ili 2 za Swappko
SAJT_ZA_SKRAPOVANJE = 2

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
    "Accept-Language": "sr-RS,sr;q=0.9,en-US;q=0.8,en;q=0.7",
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"
}

svi_oglasi = []
UKUPNO_STRANICA = 10  # Podešavanje broja stranica

if SAJT_ZA_SKRAPOVANJE == 1:
    BASE_URL = "https://dajnadan.rs/oglasi"
    DOMEN = "https://dajnadan.rs"
    IME_FAJLA = "dajnadan_oglasi.xlsx"
else:
    BASE_URL = "https://swappko.com/browse"
    DOMEN = "https://swappko.com"
    IME_FAJLA = "swappko_oglasi.xlsx"

print(f"Započinjem prikupljanje podataka sa sajta: {DOMEN} ...")

for stranica in range(1, UKUPNO_STRANICA + 1):
    url_stranice = f"{BASE_URL}?page={stranica}"
    
    print(f"Učitavam stranicu {stranica} od {UKUPNO_STRANICA}... ({url_stranice})")
    
    try:
        odgovor = requests.get(url_stranice, headers=HEADERS, timeout=15)
        
        if odgovor.status_code != 200:
            print(f"Greška pri učitavanju stranice {stranica}. Status kod: {odgovor.status_code}")
            continue
            
        soup = BeautifulSoup(odgovor.text, 'html.parser')
        
        # --- PRONALAŽENJE KARTICA ---
        if SAJT_ZA_SKRAPOVANJE == 1:
            # Logika za Dajnadan
            potencijalne_kartice = soup.find_all('div', class_='p-3')
            kartice_oglasa = [k for k in potencijalne_kartice if k.find('h3')]
        else:
            # Logika za Swappko (Na osnovu novog HTML-a)
            # Kartica je <a> tag koji u klasi ima 'bg-card' i 'group'
            kartice_oglasa = soup.find_all('a', class_=lambda c: c and 'bg-card' in c and 'group' in c)
            
        if not kartice_oglasa:
            print(f"Upozorenje: Na stranici {stranica} nisu pronađeni oglasi.")
            continue
            
        for kartica in kartice_oglasa:
            try:
                if SAJT_ZA_SKRAPOVANJE == 1:
                    # --- PARSIRANJE DAJNADAN ---
                    naslov_el = kartica.find('h3')
                    naslov = naslov_el.text.strip() if naslov_el else "Nema naslova"
                    
                    korisnik_el = kartica.find('button')
                    korisnik = korisnik_el.text.strip() if korisnik_el else "Nema korisnika"
                    
                    lokacija_el = kartica.find('p', class_='text-gray-500')
                    lokacija = lokacija_el.text.strip() if lokacija_el else "Nema lokacije"
                    
                    cena_iznos_el = kartica.find('span', class_='text-rose-600')
                    cena_iznos = cena_iznos_el.text.strip() if cena_iznos_el else ""
                    cena_valuta_el = kartica.find('span', class_='text-gray-900') 
                    cena_valuta = cena_valuta_el.text.strip() if cena_valuta_el else ""
                    cena = f"{cena_iznos} {cena_valuta}".strip() if cena_iznos else "Dogovor"
                    
                    link_el = kartica.find_parent('a')
                    link = link_el['href'] if link_el and 'href' in link_el.attrs else "Nema linka"
                    
                else:
                    # --- PARSIRANJE SWAPPKO ---
                    naslov_el = kartica.find('h3')
                    naslov = naslov_el.text.strip() if naslov_el else "Nema naslova"
                    
                    korisnik = "N/A" # U ovom dizajnu nema imena korisnika direktno na kartici
                    
                    lokacija_el = kartica.find('span', class_='truncate')
                    lokacija = lokacija_el.text.strip() if lokacija_el else "Nema lokacije"
                    
                    # Pronalazi tag za cenu (sadrži font-bold i text-foreground)
                    cena_iznos_el = kartica.find('span', class_=lambda c: c and 'font-bold' in c and 'text-foreground' in c)
                    cena_iznos = cena_iznos_el.text.strip() if cena_iznos_el else ""
                    
                    # Pronalazi tag za "/ dan"
                    cena_valuta_el = kartica.find('span', class_=lambda c: c and 'text-muted-foreground' in c and 'ml-1' in c)
                    cena_valuta = cena_valuta_el.text.strip() if cena_valuta_el else ""
                    cena = f"{cena_iznos} {cena_valuta}".strip() if cena_iznos else "Dogovor"
                    
                    link = kartica.get('href', 'Nema linka')
                
                # Sređivanje relativnih linkova
                if link.startswith('/'):
                    link = DOMEN + link

                # Dodavanje u listu
                svi_oglasi.append({
                    "Naslov": naslov,
                    "Korisnik/Izdavač": korisnik,
                    "Cena": cena,
                    "Lokacija": lokacija,
                    "Link": link
                })
                
            except Exception as e:
                print(f"Greška pri obradi pojedinačnog oglasa: {e}")
                continue
                
    except Exception as e:
        print(f"Došlo je do greške na stranici {stranica}: {e}")
        
    time.sleep(random.uniform(1.5, 3.0))

# --- ČUVANJE U EXCEL ---
if svi_oglasi:
    df = pd.DataFrame(svi_oglasi)
    df.to_excel(IME_FAJLA, index=False)
    print(f"\nUspešno sačuvano {len(svi_oglasi)} oglasa u fajl '{IME_FAJLA}'.")
else:
    print("\nNije pronađen nijedan oglas.")
    print("MOGUĆI PROBLEM: Ako su klase sada 100% tačne, a oglasi se i dalje ne prikazuju, Swappko verovatno koristi JavaScript (React/Next.js) za učitavanje podataka tek nakon što se otvori pretraživač.")