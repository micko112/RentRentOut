# IzdajemIznajmljujem

A full-stack peer-to-peer rental marketplace where users can list items for rent and book rentals from others. Live at **[izdajemiznajmljujem.com](https://izdajemiznajmljujem.com)**.

---

## Features

### Core
- **Ad listings** — Create, edit and browse rental listings with image galleries (up to 10 photos via Cloudinary), tiered pricing (daily/weekly/monthly), location and availability calendar
- **Search & filtering** — Filter by category, city, price range and rental interval; sort by newest / cheapest / most expensive; debounced preview count in filter sidebar
- **Rental contracts** — Full lifecycle: `REQUESTED → ACCEPTED → ACTIVE → FINISHED / CANCELLED`; automatic status transitions via scheduled job
- **Rental calendar** — Visual availability calendar embedded in ad details and chat; owners can block dates manually
- **Real-time chat** — WebSocket (STOMP) messaging; system messages auto-generated on contract events; contract request card in chat thread
- **Review system** — Mutual rating after completed rentals (up to 30 days post-contract); requires a shared finished contract; 3-question form (payment, communication, agreement) → POSITIVE / NEGATIVE
- **In-app notifications** — Contract events, new reviews and saved-ad alerts; unread badge in sidebar
- **Save / bookmark** ads with live save-count tracking

### User & Auth
- **Authentication** — HttpOnly cookie JWT (XSS-safe, no localStorage); `access_token` (15 min) + `refresh_token` (7 days); auto-refresh on 401
- **Social login** — Google (GIS button), Facebook SDK, Apple identity token
- **Email verification** + password reset via HTML email
- **Phone number encryption** — AES-256/CBC in DB; masked in public API (`06x / xxx-xxxx`), revealed only on authenticated request
- **User profiles** — Public profile with ads tab + reviews tab (filterable by role / sentiment); avatar upload via Cloudinary
- **Rate limiting** — Bucket4j per-IP throttling on auth and social login endpoints

### Monetisation
- **Promotion system** — Three packages: Featured (500 RSD / 7 days, rank 3), Priority (250 RSD / 3 days, rank 2), Highlighted (100 RSD / 30 days, visual only); promotes ads to top of search results
- **Credit system** — Platform credits; admin tops up via admin panel; deducted on promotion activation; full transaction history
- **Ad expiry** — Ads active for 30 days; free renewal any time; email reminder 3 days before expiry; automatic archival at 03:00 daily

### Admin
- **Dashboard** — 6 stat cards: users, total ads, active ads, contracts, active contracts, pending reports
- **User management** — List, enable/disable accounts, add credit
- **Ad management** — List, suspend/activate
- **Reports** — View ad reports (filter unreviewed), mark as reviewed; unreviewed count shown on dashboard

### Platform
- **HTML emails** — Purple-themed table-based layout; all in Serbian; 7 templates: verification, password reset, contract request/accept/reject, credit added, ad expiry reminder
- **SEO** — Dynamic `<title>` + Open Graph + Twitter Card meta on ad detail pages; `sitemap.xml` generated from all active ad IDs; `robots.txt`
- **Cookie consent** — GDPR banner; GA4 loaded only after consent; `localStorage`-persisted choice
- **Legal pages** — Privacy policy, Terms of service, How it works, Contact (GDPR requests)
- **PWA** — `manifest.webmanifest` with `theme_color: #813181`, standalone display, icons
- **Monitoring** — Sentry (backend: `sentry-spring-boot-starter-jakarta`; frontend: `@sentry/angular`)
- **Analytics** — Google Analytics 4 (`G-GYYJSDLKLB`), loaded dynamically on cookie consent

---

## Tech Stack

### Backend

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.2.4 (Java 17, Maven) |
| Database | MySQL 8.0 + Liquibase migrations |
| ORM | Spring Data JPA (Hibernate) |
| Security | Spring Security, JJWT 0.11.5 |
| WebSocket | Spring WebSocket + STOMP |
| Images | Cloudinary SDK |
| Rate limiting | Bucket4j 8.10.1 |
| Social auth | Google API Client 2.2.0, Nimbus JOSE JWT 9.37.3 (Apple) |
| XSS sanitization | jsoup 1.18.1 |
| Monitoring | Sentry Spring Boot Starter |
| API Docs | springdoc-openapi (Swagger UI at `/swagger-ui.html`) |

### Frontend

| Layer | Technology |
|---|---|
| Framework | Angular 19.2 (TypeScript 5.7) |
| Styling | Custom CSS (no UI framework) |
| Icons | Google Material Icons / Material Symbols Outlined |
| WebSocket | @stomp/rx-stomp 2.3 |
| Reactive | RxJS 7.8 |
| Monitoring | @sentry/angular 10 |

### Infrastructure

| Component | Details |
|---|---|
| Hosting | Hetzner CX22 VPS (Ubuntu 22.04) |
| Reverse proxy | Nginx |
| SSL | Let's Encrypt (auto-renewal cron at 03:00) |
| Containers | Docker Compose |
| Images | Cloudinary (cloud: `drwxucq4m`) |
| Mail | Gmail SMTP (`izdajemiznajmljujem.rs@gmail.com`) |
| Backups | Daily MySQL dump at 02:00, gzip, 14-day rotation |

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                     Nginx (HTTPS :443)                   │
│   /api/* → backend   /ws → backend   /* → frontend      │
└────────────────────┬────────────────────────────────────┘
                     │
        ┌────────────┴────────────┐
        │                        │
┌───────▼────────┐      ┌────────▼───────┐
│  Angular SPA   │      │  Spring Boot   │
│  :4200 / Nginx │      │  :8080         │
│                │      │                │
│ Feature modules│      │ REST API       │
│ Lazy-loaded    │      │ WebSocket /ws  │
└────────────────┘      └────────┬───────┘
                                 │
                        ┌────────▼───────┐
                        │   MySQL 8.0    │
                        │   :3306        │
                        └────────────────┘
```

### Backend Package Structure

```
org.landm/
├── controller/     # REST endpoints + WebSocket controller
├── service/        # Business logic (interfaces + impl/)
├── repository/     # Spring Data JPA repositories
├── entity/         # JPA entities + Enums
├── dto/            # Request/response DTOs (grouped by feature)
├── mapper/         # Entity ↔ DTO mappers
├── security/       # JWT, filters, WebSocket interceptor, phone AES encryption
├── config/         # CORS, WebSocket, mail config
└── exception/      # Custom exceptions
```

### Frontend Structure

```
src/app/
├── core/
│   ├── config/        # API endpoints, RxStomp config
│   ├── layout/        # App shell (Header, Navbar, Sidebar, Footer)
│   └── services/      # NotificationService (chat unread badge)
├── shared/            # TypeScript models, Toast, SkeletonCard, pipes
└── features/          # Lazy-loaded modules
    ├── auth/          # Login, Register (ToS checkbox), email verify, password reset
    ├── ads/           # Listings, details, create/edit wizard, RentalCalendar, PromotionModal
    ├── chat/          # Real-time inbox (3-column: conversations | messages | calendar)
    ├── user/          # Profile, my-ads (expiry + promo badges), saved-ads, contracts, credit
    ├── review/        # Rating form + review cards
    ├── notifications/ # Notification center
    ├── legal/         # Privacy policy, Terms of service, How it works, Contact
    └── admin/         # Dashboard, users, ads, contracts, reports, credits
```

---

## Getting Started

### Prerequisites

- Java 17+
- Node.js 20+ / npm
- MySQL 8.0 (or Docker)

### Option 1 — Docker Compose (recommended)

```bash
git clone https://github.com/your-username/rent-rent-out.git
cd rent-rent-out
docker-compose up --build
```

Services:
- Frontend: http://localhost:4200
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

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
npm start   # proxies /api and /ws → localhost:8080
```

---

## Configuration

The backend requires `application.properties` (local) or environment variables. Minimum required:

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

# Phone encryption
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

# Frontend base URL (for CORS + email links)
app.frontend.base-url=http://localhost:4200

# Sentry (optional)
sentry.dsn=...
sentry.traces-sample-rate=0.1
sentry.environment=local
```

> **Production**: `application.properties` is in `.gitignore`. The server uses `application-prod.properties` with `--spring.profiles.active=prod`. Sensitive values are in `/opt/app/RentRentOut/.env`.

---

## Authentication Flow

1. Login → sets two **HttpOnly cookies**: `access_token` (15 min) + `refresh_token` (7 days)
2. Login response JSON contains `wsToken` (short-lived) — stored **in-memory only** for WebSocket STOMP auth header
3. `errorInterceptor` catches 401 → calls `POST /api/auth/refresh` (refresh cookie sent automatically) → retries original request
4. Logout → `POST /api/auth/logout` clears both cookies (`maxAge=0`)

---

## API Overview

| Resource | Endpoints |
|---|---|
| Auth | `POST /api/auth/login`, `/refresh`, `/logout`, `GET /api/auth/ws-token` |
| Users | `GET/PUT /api/user/me`, `GET /api/user/{id}`, `GET /api/user/{id}/phone` |
| Ads | `GET/POST /api/ads`, `GET/PUT/DELETE /api/ads/{id}`, `GET /api/ads/search` |
| Contracts | `GET/POST /api/contracts`, `PATCH /api/contracts/{id}/status` |
| Reviews | `POST /api/reviews`, `GET /api/user/{id}/reviews`, `GET /api/reviews/contract-with/{userId}` |
| Chat | `GET /api/chat/conversations`, `GET /api/chat/{id}/messages`, `GET /api/chat/unread-count` |
| WebSocket | `STOMP /ws` → `/queue/messages`, `/queue/notifications` |
| Notifications | `GET /api/notifications`, `PATCH /api/notifications/{id}/read`, `PATCH /api/notifications/read-all` |
| Promotions | `GET /api/promotions/packages`, `POST /api/promotions/activate`, `POST /api/promotions/renew/{adId}`, `GET /api/promotions/credit` |
| Reports | `POST /api/ads/{id}/report` |
| Categories | `GET /api/categories` |
| Locations | `GET /api/locations` |
| Admin | `GET /api/admin/stats`, `/users`, `/ads`, `/contracts`, `/reports`, `POST /api/promotions/admin/credit` |
| SEO | `GET /sitemap.xml` |

Full interactive docs: `/swagger-ui.html` (when running locally).

---

## Database Migrations

Schema managed with **Liquibase**. Files in:

```
RentRentOut/src/main/resources/db/changelog/
```

Changesets 1–24 are applied. **Never edit existing changesets** — always add new numbered XML files.

---

## Running Tests

```bash
# Backend (57 unit tests)
cd RentRentOut
mvn test

# Frontend
cd RentRentOutFront/rent-rent-out-front
npm test
```

---

## Deployment

Production on Hetzner VPS (`/opt/app/`):

```bash
cd /opt/app
git pull
docker compose -f docker-compose.prod.yml up --build -d
```

SSL auto-renewed by cron at 03:00. DB backed up daily at 02:00 (`backup.sh`, 14-day rotation).

---

## Use Cases

72 use cases across 8 domains — see [`slucajevi koriscenja.txt`](slucajevi%20koriscenja.txt).

---

## License

MIT
