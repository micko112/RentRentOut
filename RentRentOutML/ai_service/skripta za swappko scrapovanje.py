import requests
import pandas as pd
import time
import re

# --- TAČAN KLJUČ SA SAJTA SWAPPKO ---
API_KLJUC = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im5ydXJvZGhldnp5cWtseXNobWdnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzQ3MTU1NDksImV4cCI6MjA5MDI5MTU0OX0.x8TgEFzjwKi0IRykPp5q4XPCtXHPz5u472ZARLLZGnM"

# Link API-ja podešen da skida po 100 oglasa umesto 12
BASE_API_URL = "https://nrurodhevzyqklyshmgg.supabase.co/rest/v1/listings?select=id,title,price_per_day,price_unit,price_on_request,base_location,category,images,quantity,is_active,promoted_until&is_active=eq.true&order=created_at.desc&limit=100"

# Zaglavlja koja uveravaju server da je zahtev legitiman
HEADERS = {
    "apikey": API_KLJUC,
    "Authorization": f"Bearer {API_KLJUC}",
    "Accept": "application/json",
    "Origin": "https://swappko.com",
    "Referer": "https://swappko.com/",
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/151.0.0.0 Safari/537.36"
}

svi_oglasi = []
offset = 0  # Početna tačka

print("Započinjem super-brzo preuzimanje podataka preko API-ja...")

while True:
    # Dodajemo "offset" u URL (0, pa 100, pa 200...) kako bismo listali "stranice" u bazi
    api_url = f"{BASE_API_URL}&offset={offset}"
    print(f"Preuzimam oglase od {offset} do {offset + 100}...")
    
    try:
        odgovor = requests.get(api_url, headers=HEADERS, timeout=15)
        
        # Provera da li nas je server blokirao
        if odgovor.status_code != 200:
            print(f"Greška! Server je odbio zahtev. Status: {odgovor.status_code}")
            print("Poruka servera:", odgovor.text)
            break
            
        podaci = odgovor.json()
        
        # Ako je lista prazna, znači da smo preuzeli sve oglase i došli do kraja
        if len(podaci) == 0:
            print("\nSvi oglasi su uspešno preuzeti!")
            break
            
        # Formatiranje i čuvanje podataka
        for oglas in podaci:
            naslov = oglas.get("title", "Nema naslova")
            id_oglasa = oglas.get("id", "")
            
            # Pravimo čist link do oglasa kakav je na sajtu
            slug = re.sub(r'[^a-z0-9]+', '-', naslov.lower()).strip('-')
            link = f"https://swappko.com/iznajmljivanje/{slug}-{id_oglasa}"
            
            # Računanje cene
            if oglas.get("price_on_request"):
                cena = "Na upit"
            else:
                cena = f"{oglas.get('price_per_day')} RSD / {oglas.get('price_unit')}"
            
            # Uzimanje prve slike
            slike = oglas.get("images", [])
            glavna_slika = slike[0] if slike else "Nema slike"
            
            # Dodavanje oglasa u listu
            svi_oglasi.append({
                "Naslov": naslov,
                "Kategorija": oglas.get("category", "N/A"),
                "Lokacija": oglas.get("base_location", "N/A"),
                "Cena": cena,
                "Link": link,
                "Glavna Slika": glavna_slika
            })
            
        # Prelazimo na sledećih 100
        offset += 100
        time.sleep(1)  # Sekunda pauze da ne dobijemo ban
        
    except Exception as e:
        print(f"Došlo je do greške: {e}")
        break

# --- ČUVANJE PODATAKA U EXCEL ---
if svi_oglasi:
    df = pd.DataFrame(svi_oglasi)
    ime_fajla = "Swappko_Kompletni_Oglasi.xlsx"
    df.to_excel(ime_fajla, index=False)
    print(f"BRAVO! Uspešno sačuvano ukupno {len(svi_oglasi)} oglasa u fajl '{ime_fajla}'.")
else:
    print("\nNijedan oglas nije prikupljen.")