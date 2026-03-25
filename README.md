# IzdajemiZnajmljujem

A full-stack peer-to-peer rental marketplace where users can list items for rent and book rentals from others. Live at **[izdajemiznajmljujem.com](https://izdajemiznajmljujem.com)**.

---

## Features

- **Ad listings** — Create, edit and browse rental listings with image galleries (up to 10 photos via Cloudinary), pricing, location and availability calendar
- **Search & filtering** — Filter by category, city, price range and rental interval; sort by newest / cheapest / most expensive
- **Rental contracts** — Full lifecycle: request → accept/reject → active → completed/cancelled, with automatic status transitions via scheduler
- **Real-time chat** — WebSocket (STOMP) messaging between renters and owners; system messages auto-generated on contract events
- **Review system** — Mutual rating after completed rentals
- **In-app notifications** — Contract events, new reviews and saved-ad alerts
- **Save / bookmark** ads with live save-count tracking
- **Authentication** — HttpOnly cookie JWT (XSS-safe), email verification, password reset, social login (Google, Facebook, Apple)
- **Rate limiting** — Bucket4j per-IP throttling on sensitive endpoints
- **Admin dashboard** — Moderation panel for managing ads and users
- **Phone number encryption** — AES-256/CBC stored in DB; masked in public API, revealed only on authenticated request

---

## Tech Stack

### Backend

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.2.4 (Java 17) |
| Database | MySQL 8.0 + Liquibase migrations |
| ORM | Spring Data JPA (Hibernate) |
| Security | Spring Security, JJWT 0.11.5 |
| WebSocket | Spring WebSocket + STOMP |
| Images | Cloudinary SDK |
| Rate limiting | Bucket4j 8.10.1 |
| Social auth | Google API Client, Nimbus JOSE JWT (Apple) |
| XSS sanitization | jsoup 1.18.1 |
| Docs | springdoc-openapi (Swagger UI at `/swagger-ui.html`) |

### Frontend

| Layer | Technology |
|---|---|
| Framework | Angular 19.2 (TypeScript 5.7) |
| Styling | Custom CSS (no UI framework) |
| Icons | Google Material Icons / Material Symbols |
| WebSocket | @stomp/rx-stomp 2.3 |
| Reactive | RxJS 7.8 |

### Infrastructure

| Component | Details |
|---|---|
| Hosting | Hetzner CX22 VPS (Ubuntu 22.04) |
| Reverse proxy | Nginx |
| SSL | Let's Encrypt (auto-renewal) |
| Containers | Docker Compose |
| Images | Cloudinary |
| Mail | Gmail SMTP |

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                     Nginx (HTTPS :443)                   │
│              /api/* → backend   /* → frontend            │
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
├── entity/         # JPA entities
├── dto/            # Request/response DTOs (grouped by feature)
├── mapper/         # Entity ↔ DTO mappers
├── security/       # JWT, filters, WebSocket interceptor, phone encryption
├── scheduler/      # Rental contract auto-transitions
└── config/         # CORS, WebSocket, mail config
```

### Frontend Structure

```
src/app/
├── core/
│   ├── config/     # API endpoints, RxStomp config
│   ├── layout/     # App shell (Navbar, Sidebar, Footer)
│   └── services/   # NotificationService (chat unread badge)
├── shared/         # TypeScript models, reusable components (Toast, SkeletonCard)
└── features/       # Lazy-loaded modules
    ├── auth/       # Login, Register, email verify, password reset
    ├── ads/        # Listings, details, create/edit wizard, rental calendar
    ├── chat/       # Real-time inbox (3-column layout)
    ├── user/       # Profile, my-ads, saved-ads, contracts
    ├── review/     # Rating & reviews
    ├── notifications/ # Notification center
    └── admin/      # Admin dashboard
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

Services start at:
- Frontend: http://localhost:4200
- Backend: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

### Option 2 — Manual

**Backend**

```bash
cd RentRentOut
# Create application.properties (see Configuration section)
mvn spring-boot:run
```

**Frontend**

```bash
cd RentRentOutFront/rent-rent-out-front
npm install
npm start   # proxies /api → localhost:8080
```

---

## Configuration

The backend requires an `application.properties` (local) or environment variables. Minimum required:

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

# Mail
spring.mail.username=...
spring.mail.password=...

# Frontend base URL (for CORS)
app.frontend.base-url=http://localhost:4200
```

> **Production note**: `application.properties` is in `.gitignore`. The server uses `application-prod.properties` with `--spring.profiles.active=prod`.

---

## Authentication

The app uses **HttpOnly cookie JWTs** (XSS-safe — no localStorage):

1. Login sets two HttpOnly cookies: `access_token` (15 min) + `refresh_token` (7 days)
2. A short-lived `wsToken` is returned in the JSON response body — stored in-memory only, used for WebSocket STOMP authentication
3. `errorInterceptor` auto-refreshes the access token on 401 responses
4. Social login supported: **Google**, **Facebook**, **Apple**

---

## API Overview

| Resource | Base path |
|---|---|
| Auth | `POST /api/auth/login`, `/refresh`, `/logout` |
| Users | `GET/PUT /api/user/me`, `GET /api/user/{id}` |
| Ads | `GET/POST /api/ads`, `GET/PUT/DELETE /api/ads/{id}` |
| Contracts | `GET/POST /api/contracts`, `PATCH /api/contracts/{id}/status` |
| Reviews | `GET/POST /api/reviews` |
| Chat | `GET /api/chat/conversations`, `GET /api/chat/{id}/messages` |
| WebSocket | `STOMP /ws` |
| Notifications | `GET /api/notifications`, `PATCH /api/notifications/read-all` |
| Categories | `GET /api/categories` |
| Admin | `GET/DELETE /api/admin/**` |

Full interactive docs available at `/swagger-ui.html` when running locally.

---

## Database Migrations

Schema is managed with **Liquibase**. Migration files are in:

```
RentRentOut/src/main/resources/db/changelog/
```

**Never edit existing changesets.** Always add new schema changes as new numbered XML files.

---

## Running Tests

```bash
# Backend
cd RentRentOut
mvn test

# Frontend
cd RentRentOutFront/rent-rent-out-front
npm test
```

---

## Deployment

Production deployment on Hetzner VPS:

```bash
cd /opt/app
git pull
docker compose -f docker-compose.prod.yml up --build -d
```

SSL is managed by Let's Encrypt with auto-renewal via cron.

---

## License

MIT
