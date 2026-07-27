# Architecture

## Overview

IzdajemIznajmljujem is a full-stack monorepo made up of **four services** orchestrated by Docker Compose:

```
+---------------------------------------------------------------------+
|                       Client (Browser / PWA)                        |
+------------------------------+--------------------------------------+
                               | HTTPS :443
                               v
+---------------------------------------------------------------------+
|                            Nginx                                    |
|                                                                     |
|  /                     -> Angular SPA (static from dist/)           |
|  /api/predict-category,                                             |
|  /api/chatbot          -> ml-service:8000                           |
|  /sitemap.xml          -> backend:8080                              |
|  /api/**               -> backend:8080                              |
|  /ws                   -> backend:8080 (WebSocket upgrade)          |
+---------------------------------------------------------------------+
                               |
            +------------------+------------------+
            v                  v                  v
   +-----------------+ +--------------+ +------------------+
   |  Spring Boot    | |   FastAPI    | |  Angular dist    |
   |  :8080          | |   :8000      | |  (static)        |
   |                 | |              | |                  |
   |  REST API       | |  /predict-   | |  lazy modules    |
   |  WebSocket      | |   category   | |  optional SSR    |
   |  Scheduler      | |  /chatbot    | |                  |
   +--------+--------+ +------+-------+ +------------------+
            |                 |
            v                 v
   +-----------------+  +------------------------------+
   |  MySQL 8.0      |  |  OpenAI API + Chroma (local) |
   |  :3306          |  |  (chatbot only)              |
   +-----------------+  +------------------------------+
```

## External services

| Service | Purpose |
|---|---|
| **Cloudinary** | Storage for ad images, avatars and chat attachments |
| **Gmail SMTP** | All transactional HTML emails |
| **OpenAI** | Embeddings (`text-embedding-3-small`) and LLM (`gpt-4o-mini`) for the chatbot |
| **Sentry** | Backend and frontend error tracking |
| **Google Analytics 4** | Loaded only after cookie consent |
| **Google / Facebook / Apple** | OAuth providers |

## Request flow: creating an ad with AI suggestion

```
+ User types a title in Step 1 of the wizard
|
+- Angular: debounce(800 ms) + switchMap + takeUntil(destroy$)
|           GET /api/categories/suggest?title=karcher
|
+- Spring Boot CategoryServiceImpl.suggestCategory()
|           RestClient.post("http://ml-service:8000/api/predict-category")
|                     .body({ title: "karcher" })
|
+- FastAPI main.py /api/predict-category
|           clean_text -> TF-IDF.transform -> model.forward -> argmax
|           label_encoder.inverse_transform([47]) -> 1322
|           -> { predicted_category_id: 1322 }
|
+- Spring Boot: returns 1322 to Angular
|
+- Angular: applySuggestedCategory(1322) -> visually marks the category in the tree
```

## Request flow: accepting a contract (with side effects)

```
PATCH /api/contracts/{id}/status  { status: ACCEPTED }
   |
   v
RentalContractServiceImpl.changeStatus()
   +- updates status in the database
   +- creates a SYSTEM message inside the conversation
   |   -> ChatWsController pushes over WS (STOMP /queue/messages)
   +- creates a Notification for the counterparty
   |   -> WS push (STOMP /queue/notifications)
   |   -> Push notification (Web Push API) if a PushSubscription exists
   +- sends an HTML email (HtmlEmailServiceImpl.sendContractAccepted)
       -> wrapped in try/catch, log.warn on failure (does not block the flow)
```

## Schedulers

Implemented with Spring `@Scheduled`:

| Job | Cron | Layer | Purpose |
|---|---|---|---|
| `expirePromotions()` | every 1 h | `PromotionServiceImpl` | Reset `promotionType` and `promotionRank` on expired promotions |
| `expireAds()` | `0 0 3 * * *` | `PromotionServiceImpl` | Set `adStatus=ARCHIVED` for expired ads |
| `sendExpiryReminders()` | `0 0 10 * * *` | `PromotionServiceImpl` | Email ads that expire in 2-3 days |
| Contract transitions | configured | `RentalContractScheduler` | ACCEPTED -> ACTIVE -> FINISHED |

## Backend layering

```
Controller   -->  Service (interface + impl)  -->  Repository  -->  Entity / MySQL
   ^                  |
   |                  +--> HtmlEmailService             (transactional emails)
   |                  +--> CloudinaryService            (image upload)
   |                  +--> RestClient                   (ML service call)
   |                  +--> NotificationPersistenceService (Notification + push)
   |
DTO (mapper: entity <-> DTO)
```

## Frontend layout

```
core/
  config/        -- endpoint constants, RxStomp configuration
  layout/        -- Header, Sidebar, Navbar, Footer
  services/      -- NotificationService (global unread badge)

shared/          -- models, Toast, SkeletonCard, pipes, CookieConsentService

features/
  +-- lazy-loaded modules (auth, ads, chat, user, review, notifications,
                           verification, support, legal, admin)
```

## Cross-cutting concerns

- **CORS** — `WebConfig` on the backend allows the frontend origin.
- **CSRF** — disabled (stateless JWT cookies).
- **Sessions** — `STATELESS`.
- **Logging** — `@Slf4j` in service implementation classes.
- **Tracing** — Sentry traces (`sentry.traces-sample-rate=0.1` in production).
