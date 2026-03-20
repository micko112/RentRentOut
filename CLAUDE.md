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
  - `user/` — User profile pages (`my-ads`, `saved-ads`, `contracts`, `my-profile`)
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

Full notification system for contract, review, and saved-ad events.

**Backend:**
- `NotificationType` enum (`entity/Enums/`): `CONTRACT_REQUESTED`, `CONTRACT_ACCEPTED`, `CONTRACT_REJECTED`, `CONTRACT_CANCELLED`, `CONTRACT_ACTIVE`, `CONTRACT_FINISHED`, `NEW_REVIEW`, `AD_SAVED`
- `Notification` JPA entity: `id`, `recipient` (ManyToOne User), `type` (EnumType.STRING), `title`, `message` (TEXT), `isRead` (default false), `relatedEntityId`, `relatedEntityType`, `actorName`, `createdAt`
- `NotificationPersistenceService` interface + impl (`service/`) — CRUD; depends only on `NotificationRepository` + `UserRepository` (no circular deps)
- `NotificationController` at `/api/notifications`:
  - `GET /api/notifications` — all for current user
  - `GET /api/notifications/unread-count` → `{ count: N }`
  - `PATCH /api/notifications/{id}/read`
  - `PATCH /api/notifications/read-all`
- Hooks in `RentalContractServiceImpl` (fires on create, ACCEPTED, REJECTED), `ReviewServiceImpl` (fires NEW_REVIEW), `AdServiceImpl.saveAd()` (fires AD_SAVED to owner when someone saves their ad)
- Liquibase migration: `db.changelog-13-create-notification.xml`

**Frontend:**
- `AppNotification` interface (`shared/models/`) — named to avoid conflict with browser `Notification` API
- `NotificationsService` (`features/notifications/services/`) — `unreadCount$` BehaviorSubject; `loadUnreadCount()`, `getAll()`, `markOneAsRead(id)`, `markAllAsRead()`
- `NotificationsPageComponent` (`features/notifications/pages/`) — filter tabs (Sve / Nepročitana), relative time formatting (`formatTime()`), icon+color per type, "Pogledaj →" router link per notification
- Route: `/notifications` (protected by `authGuard`)

### Save Count na oglasima

`Ad` entity ima `saveCount` kolonu (INT, default 0) koja se:
- **inkrementira** u `AdServiceImpl.saveAd()` kada korisnik sačuva oglas
- **dekrementira** u `AdServiceImpl.unsaveAd()` (minimum 0)

`AdPreviewDto` i `AdDto` oba imaju `saveCount` polje. Prikazuje se na `AdCardComponent` pored `viewCount` (ikonica `bookmark`).

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

### Edit Ad Wizard

Isti dvokokračni wizard format kao Create Ad, na `features/ads/pages/edit-ad/`:
- Step 1: Kategorija (select) + naslov (char counter) + opis (char counter)
- Step 2: Drag-drop slike (postojeće + nove) + cena/valuta/interval + lokacija autocomplete + quantity stepper
- Forma se pre-popunjava sa postojećim podacima oglasa; submit šalje PATCH zahtev

### My Ads stranica

`features/user/pages/my-ads/` — upravljanje sopstvenim oglasima:
- Prikazuje listu oglasa sa `AdCardComponent` (list view)
- **Pretraga** po naslovu (client-side filter, `searchQuery` getter `filteredAds`)
- **Material Icons** umesto emojija (`edit`, `delete_outline`, `campaign` za empty state)
- **Delete modal** — otvara se klikom na "Obriši"; sadrži razloge za brisanje (radio) + dugmad "Odustanite" / "Obrišite oglas"

### Ad List stranica i pretraga

`features/ads/pages/ad-list/` — glavna stranica sa oglasima:
- **Grid mod**: kategorijski pregled, prikazuje se `CategoriesSidebar`
- **Search mod**: aktivan kad ima keyword ili categoryId; prikazuje `FiltersSidebar` i kartice u list view-u
- **Paginacija**: numerisana (page buttons), sa `…` za preskočene opsege; trenutna stranica purple `#813181`
- `goToPage()` skroluje na vrh stranice

### FiltersSidebar

`features/ads/components/filters-sidebar/` — filter panel u search modu:
- **Keyword**: search input sa ikonom i × za brisanje
- **Kategorija**: select sa `category` ikonom
- **Grad**: select sa `location_on` ikonom; prikazuje `"Grad – Opština"` format
- **Tip zakupa**: toggle pill dugmad (Po satu / Po danu / Po mesecu) — šalje `priceInterval` na backend
- **Raspon cene**: Od — Do inputi
- **Apply dugme**: purple, prikazuje badge sa brojem aktivnih filtera
- Backend `AdServiceImpl.buildSearchSpec()` filtrira i po `priceInterval`

### AdCard komponenta

`features/ads/components/ad-card/`:
- **Grid view**: slika + naslov + lokacija + cena + view/save count
- **List view**: pored gore navedenog, prikazuje i **opis** (2 reda, ellipsis)
- `saveCount` prikazan sa `bookmark` ikonicom pored `viewCount`

### Lokacije

Lokacije se **isključivo** dodaju kroz Liquibase seed fajlove — nema Create Location endpointa.
- Seed fajl: `db.changelog-seed-location.xml` — sadrži 40+ lokacija Srbije (Beograd 14 opština, Novi Sad 6, Niš 4, Kragujevac 3, Subotica 2, ostali gradovi po jedan unos)
- `db.changelog-4-create-location.xml` — sadrži **samo** kreaciju tabele, bez insert-a
- `LocationRepository.findAllByOrderByCityAscMunicipalityAsc()` — vraća lokacije abecedno
- Location autocomplete (create-ad, edit-ad) pretražuje po gradu I opštini

### Contracts stranica

`features/user/pages/contracts/` — prikazuje dolazne i odlazne ugovore:
- **Pretraga** po naslovu oglasa (client-side, `filteredIncoming` / `filteredOutgoing` getteri)

### Color Theme

Two primary colors:
- **Purple** `#813181` — sidebar avatar, active nav links, notification badges/pills, buttons, accents
- **Green** `#6ecf7e` — secondary accent; opacity is intentionally varied per context (e.g. `rgba(110, 207, 126, 0.X)`)

**Do not replace either color with blue or other colors.**

## Communication

Uvek odgovaraj na srpskom jeziku (latinica). Korisnik je iz Srbije.
