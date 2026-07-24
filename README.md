<div align="center">

<img src="docs/screenshots/hero-banner.png" alt="IzdajemIznajmljujem banner" width="100%" />

# IzdajemIznajmljujem

**Full-stack peer-to-peer rental marketplace** with AI-driven category suggestions, a RAG chatbot, real-time chat, and a monetization system.

[![Live](https://img.shields.io/badge/live-izdajemiznajmljujem.com-813181?style=for-the-badge)](https://izdajemiznajmljujem.com)
[![Backend](https://img.shields.io/badge/Spring%20Boot-3.2.4-6DB33F?style=for-the-badge&logo=spring)](https://spring.io/projects/spring-boot)
[![Frontend](https://img.shields.io/badge/Angular-19.2-DD0031?style=for-the-badge&logo=angular)](https://angular.dev)
[![Database](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql)](https://www.mysql.com)
[![ML](https://img.shields.io/badge/PyTorch-2.x-EE4C2C?style=for-the-badge&logo=pytorch)](https://pytorch.org)
[![License](https://img.shields.io/badge/license-MIT-blue?style=for-the-badge)](#license)

[**Live demo**](https://izdajemiznajmljujem.com) · [**Wiki**](wiki/Home.md) · [**API docs**](http://localhost:8080/swagger-ui.html) · [**ML notebook**](RentRentOutML/ai_service/Neural_Network_for_Category_Recommendation.ipynb) · [**Chatbot notebook**](RentRentOutML/ai_service/LLM_Chatbot.ipynb)

</div>

---

## Screenshots

<table>
<tr>
<td width="50%">
<img src="docs/screenshots/home.png" alt="Home page" />
<p align="center"><b>Home</b> — hero banner, five categories and latest ads</p>
</td>
<td width="50%">
<img src="docs/screenshots/ad-details.png" alt="Ad details" />
<p align="center"><b>Ad details</b> — gallery, pricing, availability calendar</p>
</td>
</tr>
<tr>
<td width="50%">
<img src="docs/screenshots/chat.png" alt="Chat inbox" />
<p align="center"><b>Real-time chat</b> — STOMP/WebSocket with system messages</p>
</td>
<td width="50%">
<img src="docs/screenshots/create-ad.png" alt="Create-ad wizard" />
<p align="center"><b>Wizard</b> — two-step ad creation with AI category suggestion</p>
</td>
</tr>
<tr>
<td width="50%">
<img src="docs/screenshots/search-filters.png" alt="Search with filters" />
<p align="center"><b>Search</b> — debounced filters, sorting, pagination</p>
</td>
<td width="50%">
<img src="docs/screenshots/admin-dashboard.png" alt="Admin dashboard" />
<p align="center"><b>Admin</b> — six statistics cards, moderation, credits</p>
</td>
</tr>
</table>

> Missing images are catalogued in [`docs/screenshots/`](docs/screenshots/README.md).

---

## Table of contents

- [About the project](#about-the-project)
- [Key features](#key-features)
- [Tech stack](#tech-stack)
- [Architecture](#architecture)
- [Repository structure](#repository-structure)
- [Running locally](#running-locally)
- [Configuration](#configuration)
- [ML service](#ml-service--ai-suggestions--chatbot)
- [Testing](#testing)
- [Deployment](#deployment)
- [Documentation (Wiki)](#documentation-wiki)
- [Use cases](#use-cases)
- [License](#license)

---

## About the project

**IzdajemIznajmljujem** is a marketplace where users rent items to one another (tools, photo equipment, sports equipment, party supplies, vehicles and more) on a daily, weekly or monthly basis. It is live at **[izdajemiznajmljujem.com](https://izdajemiznajmljujem.com)**.

The project is a full-stack monorepo composed of **four services**:

```
+-----------------------------------------------------------------+
|                  Nginx (HTTPS :443, Let's Encrypt)              |
+------+-----------------+------------------+---------------------+
       |                 |                  |
   /api, /ws          /predict, /chat       /
       |                 |                  |
+------v------+    +-----v------+    +------v------+
| Spring Boot |    |  FastAPI   |    |  Angular 19 |
|   :8080     |    | ML service |    |     SPA     |
|             |    |   :8000    |    |             |
+------+------+    +------------+    +-------------+
       |
+------v------+
|  MySQL 8.0  |
|   :3306     |
+-------------+
```

---

## Key features

### Marketplace
- **Ads** — create, edit, delete, gallery of up to ten images (Cloudinary), tiered pricing (day/week/month), deposit, location.
- **Search and filters** — category, city, price range, interval; sorting (newest / cheapest / most expensive); debounced (350 ms) preview of the result count.
- **Hierarchical categories** — three levels, roughly 700 leaf categories.
- **Specialised attributes** — `Real Estate`, `Vehicles` and `Clothing` expose additional type-specific fields.
- **Ad templates** — saved templates for faster repeat postings.
- **Save / bookmark** with live save-count tracking.
- **Ad views** — per-user and per-IP view tracking with a unique constraint.

### Contracts and calendar
- **Lifecycle** — `REQUESTED -> ACCEPTED -> ACTIVE -> FINISHED / CANCELLED`.
- **Scheduler** — automatic status transitions (`RentalContractScheduler`).
- **Rental calendar** — standalone Angular component reused in `AdDetails` and `Inbox`; owners can block dates.

### Chat and notifications
- **WebSocket (STOMP)** — `JwtChannelInterceptor` authenticates each connection using a short-lived `wsToken`.
- **Three message types** — `REGULAR`, `SYSTEM` (centred grey bubble), `CONTRACT_REQUEST` (rich card with icons).
- **Attachments** — file uploads via chat (Cloudinary).
- **Notifications** — `CONTRACT_REQUESTED/ACCEPTED/REJECTED/CANCELLED/ACTIVE/FINISHED`, `NEW_REVIEW`, `AD_SAVED`.
- **Push notifications** — `PushSubscription` entity, Web Push API.
- **Polling fallback** — five-second polling when WebSocket is unavailable.
- **Unread badge** — chat and notifications, synchronised through `NotificationService`.

### Authentication and security
- **HttpOnly cookie JWT** — `access_token` (15 min) and `refresh_token` (7 days) with automatic refresh on 401.
- **Social login** — Google (GIS), Facebook (FB SDK), Apple (identity token).
- **Email verification and password reset** via HTML email.
- **Identity verification** — admin verifies real user identity (`IdentityVerification` entity).
- **AES-256 phone number encryption** — `PhoneNumberConverter` (CBC with random IV); masked in public APIs (`06x / xxx-xxxx`).
- **Rate limiting** — Bucket4j, per-IP, on authentication endpoints.
- **XSS protection** — jsoup sanitisation; HttpOnly cookies (tokens never touch JavaScript).
- **HTTP headers** — `X-Frame-Options: DENY`, HSTS (1 year), CSP.

### Monetization
| Package | Price | Duration | Rank | Effect |
|---|---|---|---|---|
| **Featured** | 500 RSD | 7 days | 3 | Top of search results |
| **Priority** | 250 RSD | 3 days | 2 | Ahead of standard listings |
| **Highlighted** | 100 RSD | 30 days | 0 | Visual highlight only |

- **Credit system** — `CreditTransaction` history; admin can top up balances.
- **Ad expiry** — 30 days, automatic archival at 03:00, email reminder two to three days before expiry.
- **Renewal** — free 30-day renewal.

### AI / ML
- **Category auto-suggest** — PyTorch MLP (four layers, 644 output classes) reaching **97.90% accuracy** on the test set; the Angular wizard sends debounced (800 ms) requests to FastAPI `/api/predict-category`.
- **RAG chatbot** — LangChain, Chroma and GPT-4o-mini; `baza_znanja.txt` is indexed with OpenAI embeddings; a LangGraph router performs a relevance check.
- **TF-IDF and PyTorch artefacts** — `tfidf_vectorizer.pkl`, `rentrentout_model.pth`, `label_encoder.pkl`.

### Admin
- **Dashboard** — six statistics cards (users, ads, active ads, contracts, active contracts, unreviewed reports).
- **Moderation** — users (enable/disable, add credit), ads (suspend/activate), reports (mark reviewed), identity verifications.
- **Reports** — `AdReport` (five reasons plus a note), duplicate guard, "unreviewed only" filter.

### Platform and SEO
- **HTML emails** — table-based purple template, XSS-safe; seven templates (verification, password reset, contract request/accept/reject, credit added, expiry reminder).
- **SEO** — dynamic `<title>`, Open Graph and Twitter card tags; `sitemap.xml` generated from all active ads; `robots.txt`.
- **PWA** — `manifest.webmanifest`, theme colour `#813181`, standalone display.
- **GDPR** — cookie banner, GA4 loaded lazily after consent, privacy policy, terms of service.
- **Sentry** — backend (`sentry-spring-boot-starter-jakarta`) and frontend (`@sentry/angular`).
- **Localisation** — UI and all emails are in Serbian (Latin script).

---

## Tech stack

### Backend
| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.2.4 (Java 17, Maven) |
| Database | MySQL 8.0 with **Liquibase** (41+ migrations) |
| ORM | Spring Data JPA (Hibernate) with JPA Specifications |
| Security | Spring Security 6, JJWT 0.11.5 |
| WebSocket | Spring WebSocket with STOMP |
| Images | Cloudinary SDK |
| Rate limiting | Bucket4j 8.10.1 |
| Social auth | Google API Client 2.2.0, Nimbus JOSE JWT 9.37.3 (Apple) |
| XSS | jsoup 1.18.1 |
| Monitoring | Sentry |
| API docs | springdoc-openapi (Swagger UI) |
| Scheduler | Spring `@Scheduled` |
| Mail | Spring Mail (Gmail SMTP) |

### Frontend
| Layer | Technology |
|---|---|
| Framework | Angular 19.2 (TypeScript 5.7), standalone components, lazy modules |
| Styling | Custom CSS (no UI framework), purple `#813181` and green `#6ecf7e` |
| Icons | Material Icons / Material Symbols Outlined |
| WebSocket | @stomp/rx-stomp 2.3 |
| Reactive | RxJS 7.8 |
| Mobile | Capacitor (`capacitor.config.ts`) |
| SSR | Angular Universal (`server.ts`) |
| Monitoring | @sentry/angular 10 |

### ML / AI
| Component | Technology |
|---|---|
| Web framework | FastAPI with Uvicorn |
| Deep learning | PyTorch 2.x (CPU) |
| Feature extraction | scikit-learn TF-IDF (10 000 features, 1-2 n-grams) |
| Serialisation | joblib |
| LLM | OpenAI GPT-4o-mini |
| Embeddings | OpenAI text-embedding-3-small |
| Vector store | Chroma (persistent) |
| Agent framework | LangChain with LangGraph |

### Infrastructure
| Component | Details |
|---|---|
| Hosting | Hetzner CX22 VPS (Ubuntu 22.04), `178.104.97.101` |
| Reverse proxy | Nginx (HTTPS :443) |
| SSL | Let's Encrypt (auto-renewal cron at 03:00) |
| Containers | Docker Compose |
| Image CDN | Cloudinary (`drwxucq4m`) |
| Mail | Gmail SMTP (`izdajemiznajmljujem.rs@gmail.com`) |
| Backups | Daily MySQL gzip dump at 02:00, 14-day rotation |
| Analytics | Google Analytics 4 (`G-GYYJSDLKLB`) |

---

## Architecture

### High level

```
+-----------------------------------------------------------------+
|                   Nginx (HTTPS :443)                            |
|  /api/*                -> backend:8080                          |
|  /ws                   -> backend:8080 (WebSocket upgrade)      |
|  /api/predict-category -> ml-service:8000                       |
|  /api/chatbot          -> ml-service:8000                       |
|  /sitemap.xml          -> backend:8080                          |
|  /*                    -> frontend (Angular dist)               |
+-----------------------------------------------------------------+
```

### Backend layers

```
Controller  -->  Service (interface + impl)  -->  Repository (JPA)  -->  Entity / MySQL
   |                       |
   |                       +--> HtmlEmailService, CloudinaryService, RestClient -> ML
   |
   +-->  DTO (mapper: entity <-> DTO)
```

### Security stack

- `SecurityConfig` — JWT filter chain, custom 401 `AuthenticationEntryPoint`.
- `JwtFilter` — reads the `access_token` cookie (falls back to the `Authorization` header).
- `JwtChannelInterceptor` — STOMP authentication using `wsToken`.
- `RateLimitFilter` — Bucket4j per-IP.
- `PhoneNumberConverter` — AES-256/CBC with a prepended random IV.

More detail: [`wiki/Authentication-and-Security.md`](wiki/Authentication-and-Security.md).

---

## Repository structure

```
Rent Rent Out/
|-- README.md                       <- this file
|-- CLAUDE.md                       <- internal documentation for Claude Code
|-- docker-compose.yml              <- local configuration (four services)
|-- docker-compose.prod.yml         <- production
|-- nginx.prod.conf                 <- Nginx reverse proxy
|-- backup.sh                       <- MySQL backup script
|-- mysql-init/                     <- initial MySQL init scripts
|-- slucajevi koriscenja.txt        <- 72 use cases
|
|-- RentRentOut/                    <- Spring Boot backend
|   |-- pom.xml
|   |-- Dockerfile
|   +-- src/main/
|       |-- java/org/landm/
|       |   |-- Main.java
|       |   |-- controller/         <- 21 REST controllers + 1 WS controller
|       |   |-- service/            <- interface + impl/
|       |   |-- repository/         <- Spring Data JPA
|       |   |-- entity/             <- 21 JPA entities + Enums/
|       |   |-- dto/                <- request / response DTOs
|       |   |-- mapper/             <- entity <-> DTO
|       |   |-- security/           <- JwtFilter, JwtUtil, PhoneNumberConverter
|       |   |-- config/             <- WebSocketConfig, MailConfig
|       |   |-- scheduler/          <- RentalContractScheduler
|       |   |-- specification/      <- JPA Specifications for search
|       |   |-- helper/             <- utilities
|       |   +-- exception/          <- custom exceptions
|       +-- resources/
|           |-- application.properties           <- .gitignored (local)
|           |-- application-docker.properties
|           |-- application-prod.properties
|           +-- db/changelog/                    <- 41+ Liquibase migrations
|
|-- RentRentOutFront/               <- Angular frontend
|   +-- rent-rent-out-front/
|       |-- package.json
|       |-- angular.json
|       |-- capacitor.config.ts     <- Capacitor (mobile)
|       |-- proxy.conf.json         <- dev proxy for /api, /ws, /api/predict-category
|       |-- nginx.conf              <- prod Nginx for Angular dist
|       |-- server.ts               <- Angular Universal SSR
|       |-- Dockerfile
|       |-- public/                 <- static assets
|       +-- src/app/
|           |-- core/
|           |   |-- config/         <- API endpoints, RxStomp config
|           |   |-- layout/         <- Header, Navbar, Sidebar, Footer
|           |   +-- services/       <- NotificationService
|           |-- shared/             <- TypeScript models, Toast, pipes, CookieConsentService
|           +-- features/           <- lazy-loaded modules
|               |-- auth/           <- login, register (ToS), verify, reset
|               |-- ads/            <- list, details, create-edit wizard, RentalCalendar, PromotionModal, ReportModal
|               |-- chat/           <- three-column inbox
|               |-- user/           <- profile, my-ads, saved-ads, contracts, credit
|               |-- review/         <- rating form and cards
|               |-- notifications/  <- notification centre
|               |-- verification/   <- identity verification flow
|               |-- support/        <- chatbot and contact
|               |-- legal/          <- privacy, ToS, how-it-works, contact
|               +-- admin/          <- dashboard, users, ads, contracts, reports, credits, verifications
|
|-- RentRentOutML/                  <- Python AI service
|   +-- ai_service/
|       |-- main.py                                                <- FastAPI app
|       |-- chatbot.py                                             <- LangGraph RAG agent
|       |-- baza_znanja.txt                                        <- chatbot knowledge base
|       |-- rentrentout_model.pth                                  <- PyTorch weights
|       |-- tfidf_vectorizer.pkl
|       |-- label_encoder.pkl
|       |-- test_modela.py
|       |-- requirements.txt
|       |-- Dockerfile
|       |-- Neural_Network_for_Category_Recommendation.ipynb       <- training notebook
|       |-- LLM_Chatbot.ipynb                                      <- chatbot notebook
|       +-- LLM Colab.ipynb
|
|-- docs/screenshots/               <- images used by the README and Wiki
+-- wiki/                           <- GitHub Wiki pages
    |-- Home.md
    |-- Architecture.md
    |-- Backend.md
    |-- Frontend.md
    |-- Database-Schema.md
    |-- ML-Service.md
    |-- Chatbot.md
    |-- Authentication-and-Security.md
    |-- API-Reference.md
    |-- Promotion-System.md
    |-- Deployment.md
    |-- Configuration.md
    +-- Use-Cases.md
```

---

## Running locally

### Prerequisites

- Docker and Docker Compose, **or**
- Java 17+, Node.js 20+, MySQL 8.0, Python 3.11.

### Option 1 — Docker Compose (recommended)

```bash
git clone https://github.com/micko112/RentRentOut.git
cd RentRentOut
docker-compose up --build
```

Services:
| URL | Description |
|---|---|
| http://localhost:4200 | Angular frontend |
| http://localhost:8080 | Spring Boot API |
| http://localhost:8080/swagger-ui.html | Swagger UI |
| http://localhost:8000 | FastAPI ML service (`/docs` for OpenAPI) |
| localhost:3306 | MySQL |

### Option 2 — Manual

**Backend**

```bash
cd RentRentOut
# Create src/main/resources/application.properties (see Configuration)
mvn spring-boot:run
```

**Frontend**

```bash
cd RentRentOutFront/rent-rent-out-front
npm install
npm start   # proxies /api, /ws, /api/predict-category -> localhost:8080/8000
```

**ML service**

```bash
cd RentRentOutML/ai_service
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000
```

---

## Configuration

The backend requires `application.properties` locally, or environment variables in production.

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
app.cookie.secure=false           # true in production (HTTPS)
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

# Frontend base URL (for CORS and email links)
app.frontend.base-url=http://localhost:4200

# ML service
ai.service.url=http://localhost:8000

# Sentry (optional)
sentry.dsn=...
sentry.traces-sample-rate=0.1
sentry.environment=local
```

Per-service detail: [`wiki/Configuration.md`](wiki/Configuration.md).

---

## ML service — AI suggestions and chatbot

Two distinct AI features are served from `RentRentOutML/ai_service/`.

### 1. Category auto-suggest (PyTorch MLP)

While the user types an ad title in the wizard, the model suggests a category in real time from a catalogue of roughly 700 leaf categories.

**Pipeline:**
```
Angular wizard (debounce 800 ms)
   |  GET /api/categories/suggest?title=...
   v
Spring Boot (CategoryServiceImpl.suggestCategory)
   |  POST http://ml-service:8000/api/predict-category  { "title": "..." }
   v
FastAPI (main.py)
   |  clean_text -> TF-IDF -> MLP forward pass -> argmax -> label_encoder.inverse_transform
   v
{ "predicted_category_id": 1322 }
```

**Model architecture:**
| Layer | Input -> Output | Activation | Dropout |
|---|---|---|---|
| 1 | TF-IDF (10 000) -> 512 | ReLU | 30% |
| 2 | 512 -> 256 | ReLU | 20% |
| 3 | 256 -> 128 | ReLU | 10% |
| 4 | 128 -> 644 classes | (CrossEntropy) | -- |

**Accuracy:** **97.90%** on the test set (20% of 12 880 synthetic ads).

Training notebook: [`Neural_Network_for_Category_Recommendation.ipynb`](RentRentOutML/ai_service/Neural_Network_for_Category_Recommendation.ipynb) — five phases: data engineering, NLP preprocessing, architecture, training, serialisation. More detail in [`wiki/ML-Service.md`](wiki/ML-Service.md).

### 2. RAG chatbot (LangChain, Chroma, GPT-4o-mini)

The chatbot answers platform questions using **retrieval-augmented generation**.

**Pipeline (LangGraph):**
```
User question
   |
   v
ROUTER (LLM) -- checks whether the question is relevant to the platform
   |
   +-- not relevant -> END (polite refusal)
   |
   +-- relevant
        v
     RETRIEVER (Chroma) -- top-3 chunks from baza_znanja.txt
        v
     GENERATOR (GPT-4o-mini) -- context-grounded answer
        v
     END
```

Notebook: [`LLM_Chatbot.ipynb`](RentRentOutML/ai_service/LLM_Chatbot.ipynb). More detail: [`wiki/Chatbot.md`](wiki/Chatbot.md).

---

## Testing

```bash
# Backend
cd RentRentOut
mvn test                              # full suite
mvn test -Dtest=AdServiceImplTest     # single test

# Frontend
cd RentRentOutFront/rent-rent-out-front
npm test
ng test --include='**/auth.service.spec.ts'

# ML service (simple smoke test)
cd RentRentOutML/ai_service
python test_modela.py
```

---

## Deployment

Production runs on a Hetzner CX22 VPS at `/opt/app/`:

```bash
cd /opt/app
git pull
docker compose -f docker-compose.prod.yml up --build -d
```

- **SSL** is auto-renewed by cron at 03:00 (`/opt/app/renew-ssl.sh`).
- **Backups** run daily at 02:00 (`backup.sh`, 14-day rotation).
- **Environment** is stored in `/opt/app/RentRentOut/.env` (symlinked from `/opt/app/.env`).

More detail: [`wiki/Deployment.md`](wiki/Deployment.md).

---

## Documentation (Wiki)

Component-level details, diagrams and explanations live in the `wiki/` folder (also pushable to the GitHub Wiki):

| Page | Contents |
|---|---|
| [Home](wiki/Home.md) | Wiki entry point and navigation |
| [Architecture](wiki/Architecture.md) | High-level diagrams, request flow, layers |
| [Backend](wiki/Backend.md) | Spring Boot details, package layout, schedulers, email |
| [Frontend](wiki/Frontend.md) | Angular feature modules, layout breakpoints, RxJS patterns |
| [Database Schema](wiki/Database-Schema.md) | 21 entities, ERD, Liquibase migrations |
| [ML Service](wiki/ML-Service.md) | PyTorch MLP, TF-IDF, training pipeline, evaluation |
| [Chatbot](wiki/Chatbot.md) | LangGraph and Chroma RAG architecture |
| [Authentication and Security](wiki/Authentication-and-Security.md) | JWT cookie flow, AES-256 phone encryption, rate limiting |
| [API Reference](wiki/API-Reference.md) | REST and WebSocket endpoints (Swagger links) |
| [Promotion System](wiki/Promotion-System.md) | Packages, credit, transactions, expiry job |
| [Deployment](wiki/Deployment.md) | VPS, Nginx, SSL, backups, environment variables |
| [Configuration](wiki/Configuration.md) | Every environment variable, per service |
| [Use Cases](wiki/Use-Cases.md) | 72 use cases (authentication, ads, contracts, chat, ...) |

**Publishing to the GitHub Wiki:**
```bash
git clone https://github.com/micko112/RentRentOut.wiki.git
cp wiki/*.md RentRentOut.wiki/
cd RentRentOut.wiki
git add . && git commit -m "Initial wiki" && git push
```

---

## Use cases

**72 use cases** are organised across eight domains. Full list: [`slucajevi koriscenja.txt`](slucajevi%20koriscenja.txt) or [`wiki/Use-Cases.md`](wiki/Use-Cases.md).

| Domain | Count | Example |
|---|---|---|
| Authentication | 8 | Registration, social login, password reset |
| Ads | 12 | Creation with AI suggestion, editing, deletion, saving |
| Search | 6 | Filters, sorting, pagination |
| Contracts | 9 | Request, acceptance, cancellation, finish |
| Chat | 7 | WebSocket messages, attachments, system cards |
| Reviews | 4 | 30-day window, mutual rating |
| Monetization | 8 | Promotions, credit, expiry |
| Admin | 10 | Dashboard, moderation, reports, identity verifications |

---

## License

[MIT](LICENSE)

---

<div align="center">
Built in Belgrade — <a href="https://izdajemiznajmljujem.com">izdajemiznajmljujem.com</a>
</div>
