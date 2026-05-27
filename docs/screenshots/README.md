# Screenshots

Ovaj folder sadrži slike koje se referenciraju iz glavnog `README.md` i iz `wiki/` stranica.

## Lista slika koje treba dodati

Da bi README izgledao kompletno, ubaci sledeće PNG/JPG fajlove u ovaj folder (istim imenima):

### Naslovne (koriste se u README hero sekciji)

| Fajl | Opis | Preporučena dimenzija |
|---|---|---|
| `hero-banner.png` | Banner/cover slika projekta (logo + naslov) | 1200×630 |
| `home.png` | Početna stranica sa hero banerom i kategorijama | 1600×900 |
| `ad-details.png` | Detalji oglasa (galerija + cena + kalendar) | 1600×900 |
| `chat.png` | Chat inbox (3 kolone: konverzacije, poruke, kalendar) | 1600×900 |
| `create-ad.png` | Wizard za kreiranje oglasa (Step 1 ili Step 2) | 1600×900 |

### Sekcije

| Fajl | Opis |
|---|---|
| `search-filters.png` | Pretraga sa FiltersSidebar i listom rezultata |
| `ad-list-home.png` | Home mod sa Najnovijim + 5 kategorija |
| `user-profile.png` | Javni profil korisnika (oglasi + recenzije) |
| `my-ads.png` | "Moji oglasi" sa promo badge-ovima i expiry info |
| `contracts.png` | Lista ugovora (incoming/outgoing) |
| `credit-page.png` | Kredit stranica (balance + paketi + istorija) |
| `notifications.png` | Notification center |
| `admin-dashboard.png` | Admin dashboard sa 6 stat kartica |
| `admin-reports.png` | Admin moderacija prijava |
| `promotion-modal.png` | Modal za biranje promotion paketa |
| `rental-calendar.png` | Komponenta za kalendar dostupnosti |
| `review-form.png` | Forma za ocenjivanje (3 pitanja) |
| `email-template.png` | Primer HTML email-a (verifikacija ili ugovor) |
| `cookie-banner.png` | GDPR cookie banner |
| `login.png` | Login stranica sa social log-in dugmadima |
| `register.png` | Registracija sa ToS checkbox-om |

### ML / AI (koriste se u wiki/ML-Service.md)

| Fajl | Opis |
|---|---|
| `ml-architecture.png` | Dijagram: Angular → Spring Boot → FastAPI ML servisa |
| `ml-training-curve.png` | Screenshot grafika tačnosti po epohama (iz Jupyter-a) |
| `ml-prediction-demo.png` | Demo predikcije u wizard-u (auto-suggest u akciji) |
| `chatbot-conversation.png` | Snimak ekrana razgovora sa chatbotom |

### Mobile

| Fajl | Opis |
|---|---|
| `mobile-home.png` | Mobilna verzija home stranice (≤ 900px) |
| `mobile-chat.png` | Mobilna verzija chat-a |

## Kako napraviti screenshots

1. Pokreni aplikaciju lokalno (`docker-compose up --build`)
2. Otvori Chrome DevTools → Device Toolbar → 1600×900 za desktop slike
3. Mobile slike: 390×844 (iPhone 14) ili 414×896
4. Za HTML email: otvori inbox, klikni email, screenshot
5. Sačuvaj PNG u ovom folderu istim imenima kao u tabeli iznad
6. Commit + push

> Ako jedna od slika fali, README/Wiki će prikazati "broken image" — to je signal da je još nisi dodao.
