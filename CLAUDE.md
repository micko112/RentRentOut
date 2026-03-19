# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Full-stack rental marketplace application:
- **Backend**: Spring Boot 3.2.4 (Java 17, Maven) — REST API + WebSocket
- **Frontend**: Angular 19.2 (TypeScript) — SPA with lazy-loaded feature modules
- **Database**: MySQL 8.0 with Liquibase migrations

## Development Commands

### Frontend (`RentRentOutFront/rent-rent-out-front/`)

```bash
npm start          # Dev server at http://localhost:4200
npm run build      # Production build to dist/
npm test           # Karma + Jasmine unit tests
ng test --include='**/foo.spec.ts'  # Run a single test file
```

### Backend (`RentRentOut/`)

```bash
mvn spring-boot:run           # Run locally (requires MySQL on localhost:3306)
mvn package -DskipTests       # Build JAR
mvn test                      # Run all tests
mvn test -Dtest=FooTest       # Run a single test class
```

### Full Stack (Docker)

```bash
docker-compose up             # Start all services (MySQL, backend :8080, frontend :4200)
docker-compose up --build     # Rebuild and start
```

## Git Worktrees

This repo uses **two worktrees**:
- `C:/xampp/htdocs/Rent Rent Out/` — `main` branch
- `C:/xampp/htdocs/RentRentOut-Profile/` — `features/user-profile` branch (this directory)

When making changes to `main`, always `cd` into `C:/xampp/htdocs/Rent Rent Out/` first.

## Architecture

### Backend (`RentRentOut/src/main/java/org/landm/`)

Layered Spring Boot architecture: Controller → Service (interface + impl) → Repository (JPA) → Entity

Key packages:
- `controller/` — REST endpoints; `ChatWsController` handles WebSocket messaging
- `service/` — Business logic; each domain has an interface and implementation
- `entity/` — JPA entities: `User`, `Ad`, `RentalContract`, `Review`, `Conversation`, `Message`, `Notification`; `MessageType` enum (`REGULAR`, `SYSTEM`)
- `dto/` — DTOs grouped by feature (ad, chat, review, user, contract, notification)
- `repository/` — Spring Data JPA repositories
- `security/` — JWT auth (`JwtUtil`, `JwtFilter`), WebSocket JWT (`JwtChannelInterceptor`), `SecurityConfig`
- `config/` — CORS (`WebConfig`), WebSocket (`WebSocketConfig`), mail

Database migrations are in `src/main/resources/db/changelog/` (Liquibase XML changesets). **Always add new schema changes as new migration files, never edit existing ones.**

Two Spring profiles: default (local) uses `application.properties`; `docker` uses `application-docker.properties`.

### Frontend (`RentRentOutFront/rent-rent-out-front/src/app/`)

Feature-based module structure with lazy loading:

- `core/` — Singleton services and layout shell
  - `config/` — API endpoint constants and RxStomp WebSocket config
  - `layout/` — Header, Footer, Navbar, Sidebar components (the app shell)
  - `services/` — `NotificationService` (global chat unread badge via `BehaviorSubject`; `initialize()` hits `GET /api/chat/unread-count`); WebSocket service for real-time chat
- `shared/` — Shared models (TypeScript interfaces) and reusable components (Toast, SkeletonCard)
- `features/` — Lazy-loaded feature modules:
  - `auth/` — Login, Register, email verification, password reset; `authGuard` protects routes
  - `ads/` — Ad listings, details, create/edit; `RentalCalendarComponent` (`features/ads/components/rental-calendar/`) is a standalone reusable calendar widget
  - `chat/` — Real-time inbox using `@stomp/rx-stomp` over WebSocket; three-column layout (conversation list | messages | calendar); system messages rendered as centered gray bubbles
  - `user/` — User profile pages
  - `contracts/` — Rental contract management
  - `review/` — Review/rating system
  - `notifications/` — In-app notification center (`/notifications` route); `NotificationsService` (`features/notifications/services/`) holds unread count via `BehaviorSubject`
  - `admin/` — Admin dashboard; `adminGuard` restricts access

Routes are defined in `app.routes.ts` with lazy loading for all feature modules.

### App Shell Layout (`app.component`)

`app.component.css` uses flexbox with `gap: 200px` between sidebar and content. The sidebar is `185px` wide and `position: sticky`. Key CSS classes:
- `.has-sidebar` — added when sidebar is visible (logged-in, non-admin routes); used to apply max-width centering on pages without sidebar
- `.is-admin` — added on `/admin` routes; removes padding from page-content

`app.component.ts` exposes:
- `showSidebar$` — based on **route only** (not auth state); `true` on all non-`/admin` routes
- `isAdmin$` — `true` when URL starts with `/admin`

`app.component.html` conditionally renders `<app-sidebar *ngIf="showSidebar$ | async">`.

Router uses `withPreloading(PreloadAllModules)` + `withInMemoryScrolling({ scrollPositionRestoration: 'top' })` — preloads all lazy chunks in the background (eliminates FOUC on first navigation) and scrolls to top on every route change.

### Sidebar (`core/layout/sidebar/`)

- Uses **Material Icons** (`<span class="material-icons nav-icon">`) — CDN loaded in `index.html`
- Color theme: purple `#813181` for avatar background, active state, hover state, and left border on active link
- Active link: `background: #f5ecff`, `border-left: 3px solid #813181`, `color: #813181`
- Unread badge (red `#e53935`) shown on Poruke and Obaveštenja links
- **Sidebar always visible** — shown on all non-admin routes regardless of login state
- **Guest state**: shows login/register buttons + locked (greyed-out, `pointer-events: none`) nav items with Material Icons
- **Logged-in state**: shows user avatar (initials), full interactive nav
- `SidebarComponent.ngOnInit()` calls both `NotificationService.initialize()` (chat unread) and `NotificationsService.loadUnreadCount()` (app notifications unread) — only when user is logged in

### Authentication Flow

JWT-based: login returns a token stored client-side → sent as `Authorization: Bearer <token>` on API calls → `JwtFilter` validates on every request. WebSocket connections authenticate via `JwtChannelInterceptor`.

### Real-Time Chat

WebSocket endpoint at `/ws` (STOMP protocol). Frontend uses `RxStompService` (configured in `core/config/`). Messages published to user-specific STOMP destinations.

`InboxComponent` (`features/chat/pages/inbox/`) uses `isLoadingMessages` flag — shows a spinner while messages load on conversation switch instead of blanking the area (prevents layout flash). Polling every 5s refreshes conversation list and active messages as WebSocket fallback.

### Message Types

`Message` entity has a `messageType` column (`VARCHAR(20)`, default `REGULAR`). Two values:
- `REGULAR` — normal user message
- `SYSTEM` — auto-generated by the backend (e.g. contract accepted/rejected); rendered in the UI as a centered italic gray bubble

System messages are created by `ChatServiceImpl.sendSystemMessage()` (called from `RentalContractServiceImpl` on status changes) and broadcast via `SimpMessagingTemplate` to both conversation participants.

### Global Chat Unread Badge

`NotificationService` (`core/services/`) holds a `BehaviorSubject<number>` for total unread chat message count:
- `initialize()` — fetches `GET /api/chat/unread-count`; called by `SidebarComponent.ngOnInit()`
- `updateFromConversations()` — recomputes from conversation list (called when Inbox loads)
- `onConversationOpened(n)` — optimistically subtracts `n` when a conversation is opened
- `onNewMessageInBackground()` — increments by 1 when a WebSocket message arrives for a background conversation

Backend: `MessageRepository.countUnreadForUser(userId)` uses a single JPQL query joining `participantOne`/`participantTwo` to avoid N+1.

### In-App Notifications System

Full notification system for contract and review events.

**Backend:**
- `NotificationType` enum (`entity/Enums/`): `CONTRACT_REQUESTED`, `CONTRACT_ACCEPTED`, `CONTRACT_REJECTED`, `CONTRACT_CANCELLED`, `CONTRACT_ACTIVE`, `CONTRACT_FINISHED`, `NEW_REVIEW`
- `Notification` JPA entity: `id`, `recipient` (ManyToOne User), `type` (EnumType.STRING), `title`, `message` (TEXT), `isRead` (default false), `relatedEntityId`, `relatedEntityType`, `actorName`, `createdAt`
- `NotificationPersistenceService` interface + impl (`service/`) — CRUD; depends only on `NotificationRepository` + `UserRepository` (no circular deps)
- `NotificationController` at `/api/notifications`:
  - `GET /api/notifications` — all for current user
  - `GET /api/notifications/unread-count` → `{ count: N }`
  - `PATCH /api/notifications/{id}/read`
  - `PATCH /api/notifications/read-all`
- Hooks in `RentalContractServiceImpl` (fires on create, ACCEPTED, REJECTED) and `ReviewServiceImpl` (fires NEW_REVIEW after save)
- Liquibase migration: `db.changelog-13-create-notification.xml`

**Frontend:**
- `AppNotification` interface (`shared/models/`) — named to avoid conflict with browser `Notification` API
- `NotificationsService` (`features/notifications/services/`) — `unreadCount$` BehaviorSubject; `loadUnreadCount()`, `getAll()`, `markOneAsRead(id)`, `markAllAsRead()`
- `NotificationsPageComponent` (`features/notifications/pages/`) — filter tabs (Sve / Nepročitana), relative time formatting (`formatTime()`), icon+color per type, "Pogledaj →" router link per notification
- Route: `/notifications` (protected by `authGuard`)

### RentalCalendarComponent

Standalone component at `features/ads/components/rental-calendar/`. Accepts:
- `@Input() ad: Ad` — the ad being viewed
- `@Input() set blockedIntervals(value)` — setter that regenerates calendar when new intervals arrive
- `@Input() isMyAd: boolean` — shows "Block dates" button instead of "Send request" when true

Reused in both `AdDetailsComponent` and `InboxComponent` (third column).

### Create Ad Wizard

Two-step wizard at `features/ads/pages/create-ad/`:
- Step 1: Category card grid + subcategory chips + title (char counter) + description (char counter)
- Step 2: Drag-drop image upload (10 images max, 10MB each), cover image selection, price/currency/interval, location autocomplete, quantity stepper
- CSS: uses `margin-left: -200px; width: calc(100% + 200px)` to cancel the app layout gap and expand to full width (reset at 900px breakpoint)

### Color Theme

Two primary colors:
- **Purple** `#813181` — sidebar avatar, active nav links, notification badges/pills, buttons, accents
- **Green** `#6ecf7e` — secondary accent; opacity is intentionally varied per context (e.g. `rgba(110, 207, 126, 0.X)`)

**Do not replace either color with blue or other colors.**
