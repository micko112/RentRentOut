# Architecture

## Pregled

IzdajemIznajmljujem je full-stack monorepo sa **četiri servisa** koja se orchestrate-uju kroz Docker Compose:

```
┌─────────────────────────────────────────────────────────────────────┐
│                       Klijent (Browser / PWA)                       │
└──────────────────────────────┬──────────────────────────────────────┘
                               │ HTTPS :443
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                            Nginx                                    │
│                                                                     │
│  /                   → Angular SPA (statika iz dist/)               │
│  /api/predict-category, /api/chatbot → ml-service:8000              │
│  /sitemap.xml        → backend:8080                                 │
│  /api/**             → backend:8080                                 │
│  /ws                 → backend:8080  (WebSocket Upgrade)            │
└─────────────────────────────────────────────────────────────────────┘
                               │
            ┌──────────────────┼──────────────────┐
            ▼                  ▼                  ▼
   ┌─────────────────┐ ┌──────────────┐ ┌──────────────────┐
   │  Spring Boot    │ │   FastAPI    │ │  Angular dist    │
   │  :8080          │ │   :8000      │ │  (statika)       │
   │                 │ │              │ │                  │
   │  REST API       │ │  /predict-   │ │  lazy modules    │
   │  WebSocket      │ │   category   │ │  SSR opcija      │
   │  Scheduler      │ │  /chatbot    │ │                  │
   └────────┬────────┘ └──────┬───────┘ └──────────────────┘
            │                 │
            ▼                 ▼
   ┌─────────────────┐  ┌──────────────────────────────┐
   │  MySQL 8.0      │  │  OpenAI API + Chroma (lokal) │
   │  :3306          │  │  (chatbot only)              │
   └─────────────────┘  └──────────────────────────────┘
```

## Eksterni servisi

| Servis | Svrha |
|---|---|
| **Cloudinary** | Skladištenje slika oglasa, avatara, chat attachments |
| **Gmail SMTP** | Svi transakcioni HTML email-ovi |
| **OpenAI** | Embeddings (`text-embedding-3-small`) + LLM (`gpt-4o-mini`) za chatbot |
| **Sentry** | Backend + frontend error tracking |
| **Google Analytics 4** | Učitava se tek nakon cookie consent-a |
| **Google / Facebook / Apple** | OAuth providers |

## Request flow: kreiranje oglasa sa AI preporukom

```
┌─ Korisnik kuca naslov u Step 1 wizard-a
│
├─ Angular: debounce(800ms) + switchMap + takeUntil(destroy$)
│           GET /api/categories/suggest?title=karcher
│
├─ Spring Boot CategoryServiceImpl.suggestCategory()
│           RestClient.post("http://ml-service:8000/api/predict-category")
│                     .body({ title: "karcher" })
│
├─ FastAPI main.py /api/predict-category
│           clean_text → TF-IDF.transform → model.forward → argmax
│           label_encoder.inverse_transform([47]) → 1322
│           → { predicted_category_id: 1322 }
│
├─ Spring Boot: vraća 1322 → Angular
│
└─ Angular: applySuggestedCategory(1322) → vizuelno označava kategoriju u stablu
```

## Request flow: prihvatanje ugovora (sa side-effect-ima)

```
PATCH /api/contracts/{id}/status  { status: ACCEPTED }
   │
   ▼
RentalContractServiceImpl.changeStatus()
   ├─ menja status u DB
   ├─ kreira SYSTEM poruku u conversation
   │   → ChatWsController šalje preko WS (STOMP /queue/messages)
   ├─ kreira Notification za drugog korisnika
   │   → WS push (STOMP /queue/notifications)
   │   → Push notification (Web Push API) ako ima PushSubscription
   └─ šalje HtmlEmail (HtmlEmailServiceImpl.sendContractAccepted)
       → try-catch, log.warn ako padne (ne blokira flow)
```

## Scheduler-i

Implementirani sa Spring `@Scheduled`:

| Job | Cron | Sloj | Šta radi |
|---|---|---|---|
| `expirePromotions()` | svakih 1h | `PromotionServiceImpl` | Resetuje `promotionType/Rank` na isteklim |
| `expireAds()` | `0 0 3 * * *` | `PromotionServiceImpl` | `adStatus=ARCHIVED` za istekle oglase |
| `sendExpiryReminders()` | `0 0 10 * * *` | `PromotionServiceImpl` | Email za oglase koji ističu za 2–3 dana |
| Contract transitions | konfigurisano | `RentalContractScheduler` | ACCEPTED → ACTIVE → FINISHED |

## Layout slojeva (backend)

```
Controller   ──▶  Service (interface + impl)  ──▶  Repository  ──▶  Entity / MySQL
   ▲                  │
   │                  ├──▶  HtmlEmailService           (transakcioni email-ovi)
   │                  ├──▶  CloudinaryService          (upload slika)
   │                  ├──▶  RestClient                 (poziv ML servisa)
   │                  └──▶  NotificationPersistenceService (Notification + push)
   │
DTO (mapper.entity↔DTO)
```

## Layout (frontend)

```
core/
  config/        — endpoint konstante, RxStomp konfiguracija
  layout/        — Header, Sidebar, Navbar, Footer
  services/      — NotificationService (globalni unread badge)

shared/          — modeli, Toast, SkeletonCard, pipes, CookieConsentService

features/
  └── lazy-loaded moduli (auth, ads, chat, user, review, notifications,
                          verification, support, legal, admin)
```

## Cross-cutting concerns

- **CORS** — `WebConfig` u backendu, dozvoljava frontend origin
- **CSRF** — disabled (stateless JWT cookies)
- **Sessions** — `STATELESS`
- **Logging** — `@Slf4j` u service impl klasama
- **Tracing** — Sentry traces (`sentry.traces-sample-rate=0.1` u produkciji)
