<div align="center">

<img src="docs/screenshots/hero-banner.png" alt="IzdajemIznajmljujem — banner" width="100%" />

# IzdajemIznajmljujem

**Full-stack peer-to-peer rental marketplace** sa AI preporukama kategorija, RAG chatbotom, real-time chat-om i sistemom monetizacije.

[![Live](https://img.shields.io/badge/live-izdajemiznajmljujem.com-813181?style=for-the-badge)](https://izdajemiznajmljujem.com)
[![Backend](https://img.shields.io/badge/Spring%20Boot-3.2.4-6DB33F?style=for-the-badge&logo=spring)](https://spring.io/projects/spring-boot)
[![Frontend](https://img.shields.io/badge/Angular-19.2-DD0031?style=for-the-badge&logo=angular)](https://angular.dev)
[![Database](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql)](https://www.mysql.com)
[![ML](https://img.shields.io/badge/PyTorch-2.x-EE4C2C?style=for-the-badge&logo=pytorch)](https://pytorch.org)
[![License](https://img.shields.io/badge/license-MIT-blue?style=for-the-badge)](#licenca)

[**🌐 Live demo**](https://izdajemiznajmljujem.com) · [**📚 Wiki**](wiki/Home.md) · [**🛠 API docs**](http://localhost:8080/swagger-ui.html) · [**🧠 ML notebook**](RentRentOutML/ai_service/Neural_Network_for_Category_Recommendation.ipynb) · [**🤖 Chatbot notebook**](RentRentOutML/ai_service/LLM_Chatbot.ipynb)

</div>

---

## 📸 Screenshots

<table>
<tr>
<td width="50%">
<img src="docs/screenshots/home.png" alt="Početna stranica" />
<p align="center"><b>Početna</b> — hero banner + 5 kategorija + najnoviji oglasi</p>
</td>
<td width="50%">
<img src="docs/screenshots/ad-details.png" alt="Detalji oglasa" />
<p align="center"><b>Detalji oglasa</b> — galerija, cena, kalendar dostupnosti</p>
</td>
</tr>
<tr>
<td width="50%">
<img src="docs/screenshots/chat.png" alt="Chat inbox" />
<p align="center"><b>Real-time chat</b> — STOMP/WebSocket + sistemske poruke</p>
</td>
<td width="50%">
<img src="docs/screenshots/create-ad.png" alt="Wizard za kreiranje oglasa" />
<p align="center"><b>Wizard</b> — 2-step kreiranje sa AI preporukom kategorije</p>
</td>
</tr>
<tr>
<td width="50%">
<img src="docs/screenshots/search-filters.png" alt="Pretraga sa filterima" />
<p align="center"><b>Pretraga</b> — debounced filteri + sortiranje + paginacija</p>
</td>
<td width="50%">
<img src="docs/screenshots/admin-dashboard.png" alt="Admin dashboard" />
<p align="center"><b>Admin</b> — 6 stat kartica, moderacija, krediti</p>
</td>
</tr>
</table>

> Slike koje fale ubaci u [`docs/screenshots/`](docs/screenshots/README.md) — lista je tamo dokumentovana.

---

## 📑 Sadržaj

- [O projektu](#-o-projektu)
- [Ključne funkcionalnosti](#-ključne-funkcionalnosti)
- [Tech stack](#-tech-stack)
- [Arhitektura](#-arhitektura)
- [Struktura repozitorijuma](#-struktura-repozitorijuma)
- [Pokretanje lokalno](#-pokretanje-lokalno)
- [Konfiguracija](#-konfiguracija)
- [ML servis](#-ml-servis--ai-preporuke--chatbot)
- [Testiranje](#-testiranje)
- [Deployment](#-deployment)
- [Dokumentacija (Wiki)](#-dokumentacija-wiki)
- [Slučajevi korišćenja](#-slučajevi-korišćenja)
- [Licenca](#-licenca)

---

## 🎯 O projektu

**IzdajemIznajmljujem** je platforma na kojoj korisnici iznajmljuju i izdaju stvari (alate, foto opremu, sportsku opremu, opremu za proslave, vozila itd.) drugima na dnevnoj/nedeljnoj/mesečnoj osnovi. Live na **[izdajemiznajmljujem.com](https://izdajemiznajmljujem.com)**.

Projekat je full-stack monorepo sa **četiri servisa**:

```
┌─────────────────────────────────────────────────────────────────┐
│                  Nginx (HTTPS :443, Let's Encrypt)              │
└──────┬─────────────────┬──────────────────┬────────────────────┘
       │                 │                  │
   /api, /ws          /predict, /chat       /
       │                 │                  │
┌──────▼──────┐    ┌─────▼──────┐    ┌──────▼──────┐
│ Spring Boot │    │  FastAPI   │    │  Angular 19 │
│   :8080     │    │ ML service │    │     SPA     │
│             │    │   :8000    │    │             │
└──────┬──────┘    └────────────┘    └─────────────┘
       │
┌──────▼──────┐
│  MySQL 8.0  │
│   :3306     │
└─────────────┘
```

---

## ✨ Ključne funkcionalnosti

### Marketplace
- **Oglasi** — kreiranje, izmena, brisanje, galerija do 10 slika (Cloudinary), tiered pricing (dan/nedelja/mesec), depozit, lokacija
- **Pretraga & filteri** — kategorija, grad, opseg cene, interval; sortiranje (najnoviji / najjeftiniji / najskuplji); debounced (350ms) preview broja rezultata
- **Hijerarhijske kategorije** — 3 nivoa (oko 700 leaf kategorija)
- **Specijalizovana polja** — `Nekretnine`, `Vozila`, `Garderoba` imaju dodatne atribute u zavisnosti od tipa
- **Ad templates** — sačuvani šabloni oglasa za brže ponovno postavljanje
- **Save / bookmark** sa live save-count tracking-om
- **Ad views** — beleženje pregleda po korisniku/IP (sa unique constraint-om)

### Ugovori & kalendar
- **Lifecycle** — `REQUESTED → ACCEPTED → ACTIVE → FINISHED / CANCELLED`
- **Scheduler** — automatske tranzicije statusa (`RentalContractScheduler`)
- **Rental calendar** — standalone Angular komponenta, ugrađena u `AdDetails` i `Inbox`; vlasnici blokiraju datume

### Chat & notifikacije
- **WebSocket (STOMP)** — `JwtChannelInterceptor` autentikuje konekciju kratkoživećim `wsToken`-om
- **Tri tipa poruka** — `REGULAR`, `SYSTEM` (centrirani sivi bubble), `CONTRACT_REQUEST` (rich kartica sa ikonama)
- **Attachments** — slanje fajlova kroz chat (Cloudinary)
- **Notifications** — `CONTRACT_REQUESTED/ACCEPTED/REJECTED/CANCELLED/ACTIVE/FINISHED`, `NEW_REVIEW`, `AD_SAVED`
- **Push notifikacije** — `PushSubscription` entity, Web Push API
- **Polling fallback** — 5s polling kad WS padne
- **Unread badge** — chat + notifikacije, sinhronizovan kroz `NotificationService`

### Autentikacija & sigurnost
- **HttpOnly cookie JWT** — `access_token` (15 min) + `refresh_token` (7 dana), auto-refresh na 401
- **Social login** — Google (GIS), Facebook (FB SDK), Apple (identity token)
- **Email verifikacija + password reset** kroz HTML email
- **Identity verification** — admin verifikuje pravu identitet korisnika (`IdentityVerification` entity)
- **AES-256 šifrovanje telefona** — `PhoneNumberConverter` (CBC + random IV); maskiran u javnim API-jima (`06x / xxx-xxxx`)
- **Rate limiting** — Bucket4j per-IP na auth endpointima
- **XSS** — jsoup sanitizacija; HttpOnly cookies (token nikad nije u JS-u)
- **HTTP headers** — `X-Frame-Options: DENY`, HSTS 1god, CSP

### Monetizacija
| Paket | Cena | Trajanje | Rank | Efekt |
|---|---|---|---|---|
| **Featured** | 500 RSD | 7 dana | 3 | Vrh pretrage |
| **Priority** | 250 RSD | 3 dana | 2 | Ispred standardnih |
| **Highlighted** | 100 RSD | 30 dana | 0 | Vizuelni highlight |

- **Kredit sistem** — `CreditTransaction` istorija, admin tops up
- **Ad expiry** — 30 dana, automatski archival u 03:00, email reminder 2–3 dana pre isteka
- **Renewal** — besplatno obnavljanje na 30 dana

### AI / ML
- **🧠 Auto-suggest kategorije** — PyTorch MLP (4 sloja, 644 izlazne klase) sa **97.90% tačnosti** na test setu; Angular wizard debounce 800ms → FastAPI `/api/predict-category`
- **🤖 RAG Chatbot** — LangChain + Chroma + GPT-4o-mini; `baza_znanja.txt` indeksiran sa OpenAI embeddings; LangGraph router za relevance check
- **TF-IDF + PyTorch** — `tfidf_vectorizer.pkl` + `rentrentout_model.pth` + `label_encoder.pkl`

### Admin
- **Dashboard** — 6 stat kartica (korisnici, oglasi, aktivni oglasi, ugovori, aktivni ugovori, neobrađene prijave)
- **Moderacija** — korisnici (enable/disable, dodaj kredit), oglasi (suspend/activate), prijave (mark reviewed), identity verifications
- **Reports** — `AdReport` (5 razloga + napomena), duplikat guard, filter "samo neobrađene"

### Platform & SEO
- **HTML email-ovi** — purple table-based template, XSS-safe; 7 šablona (verifikacija, reset lozinke, contract request/accept/reject, credit added, expiry reminder)
- **SEO** — dinamički `<title>`, OG, Twitter card; `sitemap.xml` iz svih aktivnih oglasa; `robots.txt`
- **PWA** — `manifest.webmanifest`, theme_color `#813181`, standalone
- **GDPR** — cookie banner, GA4 lazy loaded posle pristanka, privacy policy, ToS
- **Sentry** — backend (`sentry-spring-boot-starter-jakarta`) + frontend (`@sentry/angular`)
- **Internacionalizacija** — interfejs i svi email-ovi na srpskom (latinica)

---

## 🛠 Tech stack

### Backend
| Layer | Tehnologija |
|---|---|
| Framework | Spring Boot 3.2.4 (Java 17, Maven) |
| Database | MySQL 8.0 + **Liquibase** (41+ migracija) |
| ORM | Spring Data JPA (Hibernate) + JPA Specifications |
| Security | Spring Security 6, JJWT 0.11.5 |
| WebSocket | Spring WebSocket + STOMP |
| Images | Cloudinary SDK |
| Rate limiting | Bucket4j 8.10.1 |
| Social auth | Google API Client 2.2.0, Nimbus JOSE JWT 9.37.3 (Apple) |
| XSS | jsoup 1.18.1 |
| Monitoring | Sentry |
| API Docs | springdoc-openapi (Swagger UI) |
| Scheduler | Spring `@Scheduled` |
| Mail | Spring Mail (Gmail SMTP) |

### Frontend
| Layer | Tehnologija |
|---|---|
| Framework | Angular 19.2 (TypeScript 5.7), standalone components, lazy modules |
| Styling | Custom CSS (no UI framework), purple `#813181` + green `#6ecf7e` |
| Icons | Material Icons / Material Symbols Outlined |
| WebSocket | @stomp/rx-stomp 2.3 |
| Reactive | RxJS 7.8 |
| Mobile | Capacitor (`capacitor.config.ts`) |
| SSR | Angular Universal (`server.ts`) |
| Monitoring | @sentry/angular 10 |

### ML / AI
| Komponenta | Tehnologija |
|---|---|
| Web framework | FastAPI + Uvicorn |
| Deep learning | PyTorch 2.x (CPU) |
| Feature extraction | scikit-learn TF-IDF (10000 features, 1-2 ngrams) |
| Serialization | joblib |
| LLM | OpenAI GPT-4o-mini |
| Embeddings | OpenAI text-embedding-3-small |
| Vector store | Chroma (persistent) |
| Agent framework | LangChain + LangGraph |

### Infrastruktura
| Komponenta | Detalji |
|---|---|
| Hosting | Hetzner CX22 VPS (Ubuntu 22.04) — `178.104.97.101` |
| Reverse proxy | Nginx (HTTPS :443) |
| SSL | Let's Encrypt (auto-renewal cron @ 03:00) |
| Containers | Docker Compose |
| Images CDN | Cloudinary (`drwxucq4m`) |
| Mail | Gmail SMTP (`izdajemiznajmljujem.rs@gmail.com`) |
| Backups | Daily MySQL gzip dump @ 02:00, 14-day rotation |
| Analytics | Google Analytics 4 (`G-GYYJSDLKLB`) |

---

## 🏛 Arhitektura

### Visok nivo

```
┌─────────────────────────────────────────────────────────────────┐
│                   Nginx (HTTPS :443)                            │
│  /api/*              → backend:8080                             │
│  /ws                 → backend:8080 (WebSocket upgrade)         │
│  /api/predict-category → ml-service:8000                        │
│  /api/chatbot        → ml-service:8000                          │
│  /sitemap.xml        → backend:8080                             │
│  /*                  → frontend (Angular dist)                  │
└─────────────────────────────────────────────────────────────────┘
```

### Backend slojevi

```
Controller  ──▶  Service (interface + impl)  ──▶  Repository (JPA)  ──▶  Entity / MySQL
   │                       │
   │                       └──▶  HtmlEmailService, CloudinaryService, RestClient → ML
   │
   └──▶  DTO (mapper.entity↔DTO)
```

### Bezbednosni stack

- `SecurityConfig` — JWT filter chain, custom 401 `AuthenticationEntryPoint`
- `JwtFilter` — čita `access_token` cookie (fallback Authorization header)
- `JwtChannelInterceptor` — STOMP autentikacija sa `wsToken`
- `RateLimitFilter` — Bucket4j per-IP
- `PhoneNumberConverter` — AES-256/CBC, random IV prepended

Detaljnije: [`wiki/Authentication-and-Security.md`](wiki/Authentication-and-Security.md)

---

## 📁 Struktura repozitorijuma

```
Rent Rent Out/
├── README.md                       ← ova datoteka
├── CLAUDE.md                       ← interna dokumentacija (za Claude Code)
├── docker-compose.yml              ← lokalna konfiguracija (4 servisa)
├── docker-compose.prod.yml         ← produkcija
├── nginx.prod.conf                 ← Nginx reverse proxy
├── backup.sh                       ← MySQL backup skripta
├── mysql-init/                     ← inicijalni MySQL init skripte
├── slucajevi koriscenja.txt        ← 72 use case-a
│
├── RentRentOut/                    ← 🔷 Spring Boot backend
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
│       ├── java/org/landm/
│       │   ├── Main.java
│       │   ├── controller/         ← 21 REST + 1 WS controller
│       │   ├── service/            ← interface + impl/
│       │   ├── repository/         ← Spring Data JPA
│       │   ├── entity/             ← 21 JPA entity + Enums/
│       │   ├── dto/                ← request/response DTOs
│       │   ├── mapper/             ← Entity ↔ DTO
│       │   ├── security/           ← JwtFilter, JwtUtil, PhoneNumberConverter
│       │   ├── config/             ← WebSocketConfig, MailConfig
│       │   ├── scheduler/          ← RentalContractScheduler
│       │   ├── specification/      ← JPA Specifications za pretragu
│       │   ├── helper/             ← Utility
│       │   └── exception/          ← Custom exceptions
│       └── resources/
│           ├── application.properties           ← .gitignored (local)
│           ├── application-docker.properties
│           ├── application-prod.properties
│           └── db/changelog/                    ← 41+ Liquibase migracija
│
├── RentRentOutFront/               ← 🔴 Angular frontend
│   └── rent-rent-out-front/
│       ├── package.json
│       ├── angular.json
│       ├── capacitor.config.ts     ← Capacitor (mobile)
│       ├── proxy.conf.json         ← dev proxy /api + /ws + /api/predict-category
│       ├── nginx.conf              ← prod nginx za Angular dist
│       ├── server.ts               ← Angular Universal SSR
│       ├── Dockerfile
│       ├── public/                 ← static assets
│       └── src/app/
│           ├── core/
│           │   ├── config/         ← API endpoints, RxStomp config
│           │   ├── layout/         ← Header, Navbar, Sidebar, Footer
│           │   └── services/       ← NotificationService
│           ├── shared/             ← TypeScript models, Toast, pipes, CookieConsentService
│           └── features/           ← lazy-loaded modules
│               ├── auth/           ← login, register (ToS), verify, reset
│               ├── ads/            ← list, details, create-edit wizard, RentalCalendar, PromotionModal, ReportModal
│               ├── chat/           ← 3-column inbox
│               ├── user/           ← profile, my-ads, saved-ads, contracts, credit
│               ├── review/         ← rating form + cards
│               ├── notifications/  ← notification center
│               ├── verification/   ← identity verification flow
│               ├── support/        ← chatbot + kontakt
│               ├── legal/          ← privacy, ToS, how-it-works, contact
│               └── admin/          ← dashboard, users, ads, contracts, reports, credits, verifications
│
├── RentRentOutML/                  ← 🟢 Python AI servis
│   └── ai_service/
│       ├── main.py                                                ← FastAPI app
│       ├── chatbot.py                                             ← LangGraph RAG agent
│       ├── baza_znanja.txt                                        ← chatbot knowledge base
│       ├── rentrentout_model.pth                                  ← PyTorch weights
│       ├── tfidf_vectorizer.pkl
│       ├── label_encoder.pkl
│       ├── test_modela.py
│       ├── requirements.txt
│       ├── Dockerfile
│       ├── Neural_Network_for_Category_Recommendation.ipynb       ← 🧠 trening notebook
│       ├── LLM_Chatbot.ipynb                                      ← 🤖 chatbot notebook
│       └── LLM Colab.ipynb
│
├── docs/screenshots/               ← slike za README + Wiki
└── wiki/                           ← GitHub Wiki stranice
    ├── Home.md
    ├── Architecture.md
    ├── Backend.md
    ├── Frontend.md
    ├── Database-Schema.md
    ├── ML-Service.md
    ├── Chatbot.md
    ├── Authentication-and-Security.md
    ├── API-Reference.md
    ├── Promotion-System.md
    ├── Deployment.md
    ├── Configuration.md
    └── Use-Cases.md
```

---

## 🚀 Pokretanje lokalno

### Preduslov

- Docker + Docker Compose **ili**
- Java 17+, Node.js 20+, MySQL 8.0, Python 3.11

### Opcija 1 — Docker Compose (preporučeno)

```bash
git clone https://github.com/micko112/RentRentOut.git
cd RentRentOut
docker-compose up --build
```

Servisi:
| URL | Opis |
|---|---|
| http://localhost:4200 | Angular frontend |
| http://localhost:8080 | Spring Boot API |
| http://localhost:8080/swagger-ui.html | Swagger UI |
| http://localhost:8000 | FastAPI ML servis (`/docs` za OpenAPI) |
| localhost:3306 | MySQL |

### Opcija 2 — Manuelno

**Backend**

```bash
cd RentRentOut
# Kreiraj src/main/resources/application.properties (vidi Konfiguraciju)
mvn spring-boot:run
```

**Frontend**

```bash
cd RentRentOutFront/rent-rent-out-front
npm install
npm start   # proxira /api, /ws, /api/predict-category → localhost:8080/8000
```

**ML servis**

```bash
cd RentRentOutML/ai_service
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000
```

---

## ⚙️ Konfiguracija

Backend zahteva `application.properties` (lokalno) ili env varijable u produkciji.

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/rent_rent_out
spring.datasource.username=root
spring.datasource.password=root

# JWT
jwt.secret=<min-32-char-secret>
jwt.expiration=900000
jwt.refresh-expiration=604800000

# Cookies
app.cookie.secure=false           # true u produkciji (HTTPS)
app.cookie.domain=localhost

# Phone encryption (AES-256, 32 chars)
encryption.phone-key=<32-char-random-key>

# Cloudinary
cloudinary.cloud-name=...
cloudinary.api-key=...
cloudinary.api-secret=...

# Mail (Gmail SMTP)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=...
spring.mail.password=...

# Frontend base URL (za CORS + email linkove)
app.frontend.base-url=http://localhost:4200

# ML servis
ai.service.url=http://localhost:8000

# Sentry (opciono)
sentry.dsn=...
sentry.traces-sample-rate=0.1
sentry.environment=local
```

Detaljnije po servisu: [`wiki/Configuration.md`](wiki/Configuration.md).

---

## 🧠 ML servis — AI preporuke + chatbot

Dva odvojena AI feature-a, oba serviraju se iz `RentRentOutML/ai_service/`:

### 1. Auto-suggest kategorije (PyTorch MLP)

Kada korisnik kuca naslov oglasa u wizard-u, model real-time predlaže kategoriju iz kataloga od ~700 leaf kategorija.

**Pipeline:**
```
Angular wizard (debounce 800ms)
   │  GET /api/categories/suggest?title=...
   ▼
Spring Boot (CategoryServiceImpl.suggestCategory)
   │  POST http://ml-service:8000/api/predict-category  { "title": "..." }
   ▼
FastAPI (main.py)
   │  clean_text → TF-IDF → MLP forward pass → argmax → label_encoder.inverse_transform
   ▼
{ "predicted_category_id": 1322 }
```

**Arhitektura modela:**
| Sloj | Ulaz → Izlaz | Aktivacija | Dropout |
|---|---|---|---|
| 1 | TF-IDF (10000) → 512 | ReLU | 30% |
| 2 | 512 → 256 | ReLU | 20% |
| 3 | 256 → 128 | ReLU | 10% |
| 4 | 128 → 644 klasa | — (CrossEntropy) | — |

**Tačnost:** **97.90%** na test setu (20% od 12880 sintetičkih oglasa).

Trening notebook: [`Neural_Network_for_Category_Recommendation.ipynb`](RentRentOutML/ai_service/Neural_Network_for_Category_Recommendation.ipynb) — 5 faza: Data Engineering → NLP Preprocesiranje → Arhitektura → Trening → Serijalizacija. Detaljnije u [`wiki/ML-Service.md`](wiki/ML-Service.md).

### 2. RAG Chatbot (LangChain + Chroma + GPT-4o-mini)

Chatbot odgovara na pitanja o platformi koristeći **retrieval-augmented generation**.

**Pipeline (LangGraph):**
```
Korisničko pitanje
   │
   ▼
ROUTER (LLM) — proverava da li je pitanje relevantno za platformu
   │
   ├── nije relevantno → END (učtivo odbije)
   │
   └── relevantno
        ▼
     RETRIEVER (Chroma) — top-3 chunk-a iz baza_znanja.txt
        ▼
     GENERATOR (GPT-4o-mini) — odgovor sa kontekstom
        ▼
     END
```

Notebook: [`LLM_Chatbot.ipynb`](RentRentOutML/ai_service/LLM_Chatbot.ipynb). Detaljnije: [`wiki/Chatbot.md`](wiki/Chatbot.md).

---

## 🧪 Testiranje

```bash
# Backend
cd RentRentOut
mvn test                              # ceo skup
mvn test -Dtest=AdServiceImplTest    # jedan test

# Frontend
cd RentRentOutFront/rent-rent-out-front
npm test
ng test --include='**/auth.service.spec.ts'

# ML servis (jednostavan smoke test)
cd RentRentOutML/ai_service
python test_modela.py
```

---

## 🚢 Deployment

Produkcija na Hetzner CX22 VPS u `/opt/app/`:

```bash
cd /opt/app
git pull
docker compose -f docker-compose.prod.yml up --build -d
```

- **SSL** auto-renewed cron-om u 03:00 (`/opt/app/renew-ssl.sh`)
- **Backup** dnevno u 02:00 (`backup.sh`, 14-day rotacija)
- **Env** u `/opt/app/RentRentOut/.env` (symlink na `/opt/app/.env`)

Detaljnije: [`wiki/Deployment.md`](wiki/Deployment.md).

---

## 📚 Dokumentacija (Wiki)

Sve detalje, dijagrame i objašnjenja po komponentama imaš u `wiki/` folderu (ujedno se može push-ovati u GitHub Wiki):

| Stranica | Sadržaj |
|---|---|
| [Home](wiki/Home.md) | Uvodna stranica Wikija — navigacija |
| [Architecture](wiki/Architecture.md) | Visok-nivo dijagrami, request flow, slojevi |
| [Backend](wiki/Backend.md) | Spring Boot detalji, package layout, scheduleri, email |
| [Frontend](wiki/Frontend.md) | Angular feature moduli, layout breakpoint-i, RxJS pattern-i |
| [Database Schema](wiki/Database-Schema.md) | 21 entity, ERD, Liquibase migracije |
| [ML Service](wiki/ML-Service.md) | PyTorch MLP, TF-IDF, trening pipeline, evaluacija |
| [Chatbot](wiki/Chatbot.md) | LangGraph + Chroma RAG arhitektura |
| [Authentication and Security](wiki/Authentication-and-Security.md) | JWT cookie flow, AES-256 telefon, rate limiting |
| [API Reference](wiki/API-Reference.md) | REST + WS endpoint-i (Swagger linkovi) |
| [Promotion System](wiki/Promotion-System.md) | Paketi, kredit, transakcije, expiry job |
| [Deployment](wiki/Deployment.md) | VPS, Nginx, SSL, backup, env varijable |
| [Configuration](wiki/Configuration.md) | Sve env varijable po servisu |
| [Use Cases](wiki/Use-Cases.md) | 72 use case-a (autentikacija, oglasi, ugovori, chat, ...) |

**Kako objaviti u GitHub Wiki:**
```bash
git clone https://github.com/micko112/RentRentOut.wiki.git
cp wiki/*.md RentRentOut.wiki/
cd RentRentOut.wiki
git add . && git commit -m "Initial wiki" && git push
```

---

## 📋 Slučajevi korišćenja

**72 use case-a** organizovana u 8 domena. Pun spisak: [`slucajevi koriscenja.txt`](slucajevi%20koriscenja.txt) ili [`wiki/Use-Cases.md`](wiki/Use-Cases.md).

| Domen | Broj | Primer |
|---|---|---|
| Autentikacija | 8 | Registracija, social login, password reset |
| Oglasi | 12 | Kreiranje sa AI preporukom, izmena, brisanje, save |
| Pretraga | 6 | Filteri, sortiranje, paginacija |
| Ugovori | 9 | Zahtev, prihvatanje, otkazivanje, finish |
| Chat | 7 | WS poruke, attachments, sistemske kartice |
| Recenzije | 4 | 30-day prozor, mutualna ocena |
| Monetizacija | 8 | Promocije, kredit, expiry |
| Admin | 10 | Dashboard, moderacija, prijave, identity verifications |

---

## 📜 Licenca

[MIT](LICENSE)

---

<div align="center">
Made with 💜 in Belgrade — <a href="https://izdajemiznajmljujem.com">izdajemiznajmljujem.com</a>
</div>
