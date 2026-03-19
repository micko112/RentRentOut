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

## Architecture

### Backend (`RentRentOut/src/main/java/org/landm/`)

Layered Spring Boot architecture: Controller → Service (interface + impl) → Repository (JPA) → Entity

Key packages:
- `controller/` — REST endpoints; `ChatWsController` handles WebSocket messaging
- `service/` — Business logic; each domain has an interface and implementation
- `entity/` — JPA entities: `User`, `Ad`, `RentalContract`, `Review`, `Conversation`, `Message`; `MessageType` enum (`REGULAR`, `SYSTEM`, `CONTRACT_REQUEST`)
- `dto/` — DTOs grouped by feature (ad, chat, review, user, contract)
- `repository/` — Spring Data JPA repositories
- `security/` — JWT auth (`JwtUtil`, `JwtFilter`), WebSocket JWT (`JwtChannelInterceptor`), `SecurityConfig`
- `config/` — CORS (`WebConfig`), WebSocket (`WebSocketConfig`), mail

Database migrations are in `src/main/resources/db/changelog/` (21 Liquibase XML changesets). **Always add new schema changes as new migration files, never edit existing ones.** Changeset 12 added `related_contract_id BIGINT nullable` to the `message` table.

Two Spring profiles: default (local) uses `application.properties`; `docker` uses `application-docker.properties`.

### Frontend (`RentRentOutFront/rent-rent-out-front/src/app/`)

Feature-based module structure with lazy loading:

- `core/` — Singleton services and layout shell
  - `config/` — API endpoint constants and RxStomp WebSocket config
  - `layout/` — Header, Footer, Navbar, Sidebar components (the app shell); Sidebar calls `NotificationService.initialize()` on load to fetch the global unread badge count; Sidebar nav includes "Moji Ugovori" link (`/user/me/contracts`) between "Poruke" and "Obaveštenja"
  - `services/` — `NotificationService` (global unread badge via `BehaviorSubject`; `initialize()` hits `GET /api/chat/unread-count`); WebSocket service for real-time chat
- `shared/` — Shared models (TypeScript interfaces) and reusable components (Toast)
- `features/` — Lazy-loaded feature modules:
  - `auth/` — Login, Register, email verification, password reset; `authGuard` protects routes
  - `ads/` — Ad listings, details, create/edit; includes category/filter sidebars; `RentalCalendarComponent` (`features/ads/components/rental-calendar/`) is a standalone reusable calendar widget accepting `@Input() ad`, `@Input() blockedIntervals`, `@Input() isMyAd`
  - `chat/` — Real-time inbox using `@stomp/rx-stomp` over WebSocket; three-column layout (conversation list | messages | calendar); three message types rendered differently (see Message Types below)
  - `user/` — User profile pages; `ContractsComponent` (`user/pages/contracts/`) supports `?contractId=X` query param to auto-scroll and highlight a specific contract on load
  - `contracts/` — Rental contract management
  - `review/` — Review/rating system
  - `admin/` — Admin dashboard; `adminGuard` restricts access

Routes are defined in `app.routes.ts` with lazy loading for all feature modules.

### Authentication Flow

JWT-based: login returns a token stored client-side → sent as `Authorization: Bearer <token>` on API calls → `JwtFilter` validates on every request. WebSocket connections authenticate via `JwtChannelInterceptor`.

### Real-Time Chat

WebSocket endpoint at `/ws` (STOMP protocol). Frontend uses `RxStompService` (configured in `core/config/`). Messages published to user-specific STOMP destinations.

#### InboxComponent (`features/chat/pages/inbox/`)

Three-column layout: conversation list sidebar | messages area | rental calendar. Key implementation notes:
- `groupedMessages: MessageGroup[]` is what the template iterates — **always call `updateGroupedMessages()` after modifying `this.messages`**, including in `handleIncomingMessage` Scenario B (WebSocket) and `refreshActiveMessages()` (polling). Missing this call is a known bug pattern.
- Polling runs every 5 seconds via `interval(5000)` — reconnects WebSocket if needed and refreshes messages.
- "Pogledaj detalje" button on CONTRACT_REQUEST cards calls `goToContract(contractId)` which navigates to `/user/me/contracts?contractId=X`.

### Message Types

`Message` entity has a `messageType` column (`VARCHAR(20)`, default `REGULAR`). Three values:
- `REGULAR` — normal user chat bubble (green for sent, white for received)
- `SYSTEM` — auto-generated by backend (e.g. contract accepted/rejected); rendered as centered italic gray bubble
- `CONTRACT_REQUEST` — generated when a rental contract is created; rendered as a styled card with ad title, dates, total price, and "Pogledaj detalje" button

System messages are created by `ChatServiceImpl.sendSystemMessage()` (called from `RentalContractServiceImpl` on status changes) and broadcast via `SimpMessagingTemplate` to both conversation participants over WebSocket.

Contract request messages are created by `ChatServiceImpl.sendContractRequestMessage(RentalContract contract)` — called from `RentalContractServiceImpl.create()` after saving. Broadcasts to both lessee and lessor. The method accepts a full `RentalContract` object (not just an ID) to avoid an extra DB round-trip.

#### MessageDto contract fields

`MessageDto` carries extra fields for `CONTRACT_REQUEST` messages (populated by `ChatMapper.toMessageDto` via a `RentalContractRepository.findById` lookup, and also set directly in `sendContractRequestMessage`):
- `contractAdTitle` — ad title
- `contractStartDate` / `contractEndDate` — ISO date strings (e.g. `"2026-03-20"`)
- `contractTotalPrice` — `agreedPrice × amount`
- `contractCurrency` — enum name (e.g. `"RSD"`)

Frontend `Message` model (`shared/models/message.model.ts`) has matching optional fields.

### Global Unread Badge

`NotificationService` (`core/services/`) holds a `BehaviorSubject<number>` for the total unread message count:
- `initialize()` — fetches `GET /api/chat/unread-count` from backend; called by `SidebarComponent.ngOnInit()` when a user is logged in
- `updateFromConversations()` — recomputes from conversation list (called when Inbox loads)
- `onConversationOpened(n)` — optimistically subtracts `n` when a conversation is opened
- `onNewMessageInBackground()` — increments by 1 when a WebSocket message arrives for a background conversation

Backend: `MessageRepository.countUnreadForUser(userId)` uses a single JPQL query joining `participantOne`/`participantTwo` to avoid N+1.

### RentalCalendarComponent

Standalone component at `features/ads/components/rental-calendar/`. Extracted from `AdDetailsComponent` and reused in both `AdDetailsComponent` and `InboxComponent` (third column). Accepts:
- `@Input() ad: Ad` — the ad being viewed
- `@Input() set blockedIntervals(value)` — setter that regenerates the calendar when new intervals arrive
- `@Input() isMyAd: boolean` — shows "Block dates" button instead of "Send request" when true

`blockDates()` self-refreshes blocked intervals via `adService.getAdById()` after a successful block, avoiding the need for an `@Output()` emitter.

### App Layout

`app.component.html` renders: `<app-header>` → `<app-navbar>` → `.main-app-wrapper` (flex row: `<app-sidebar>` + `.page-content`).

- `.main-app-wrapper` has no `margin-top`; `.page-content` has `padding-top: 30px` so only content (not the sidebar) has the top gap
- Global sidebar (`.sidebar`) uses `position: sticky; top: 142px` (matches header height: 40px padding × 2 + 61px logo + 1px border)
- Chat container (`.chat-container`) uses `max-width: 1400px; margin-left: 40px` to give the three-column chat layout more room

### ContractsComponent Scroll-to-Contract

When navigating to `/user/me/contracts?contractId=X` (e.g. from a chat contract card), `ContractsComponent` reads the `contractId` query param on init, waits for contracts to load, then scrolls to `#contract-X` with a smooth animation and a 2-second purple pulse highlight (`contract-highlighted` CSS class).
