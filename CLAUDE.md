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
- `C:/xampp/htdocs/RentRentOut-Profile/` — `features/user-profile` branch

When making changes to `main`, always `cd` into `C:/xampp/htdocs/Rent Rent Out/` first.

## Architecture

### Backend (`RentRentOut/src/main/java/org/landm/`)

Layered Spring Boot architecture: Controller → Service (interface + impl) → Repository (JPA) → Entity

Key packages:
- `controller/` — REST endpoints; `ChatWsController` handles WebSocket messaging; `AuthController` handles `/api/auth/refresh`, `/api/auth/logout`, `/api/auth/ws-token`
- `service/` — Business logic; each domain has an interface and implementation
- `entity/` — JPA entities: `User`, `Ad`, `RentalContract`, `Review`, `Conversation`, `Message`, `Notification`; `MessageType` enum (`REGULAR`, `SYSTEM`)
- `dto/` — DTOs grouped by feature (ad, chat, review, user, contract, notification)
- `repository/` — Spring Data JPA repositories
- `security/` — JWT auth (`JwtUtil`, `JwtFilter`), WebSocket JWT (`JwtChannelInterceptor`), `SecurityConfig`, `PhoneNumberConverter` (AES-256 JPA converter)
- `config/` — CORS (`WebConfig`), WebSocket (`WebSocketConfig`), mail

Database migrations are in `src/main/resources/db/changelog/` (Liquibase XML changesets). **Always add new schema changes as new migration files, never edit existing ones.**

Two Spring profiles: default (local) uses `application.properties`; `docker` uses `application-docker.properties`; `prod` uses `application-prod.properties`.

**Ključne zavisnosti u `pom.xml`** koje nisu managed kroz Spring Boot BOM i moraju imati eksplicitnu verziju:
- `com.nimbusds:nimbus-jose-jwt:9.37.3` — koristi se u `UserServiceImpl.appleLogin()` za verifikaciju Apple JWT tokena
- `com.google.api-client:google-api-client:2.2.0` — za Google OAuth verifikaciju u `UserServiceImpl.googleLogin()`

### Security

`SecurityConfig.java` — ključne napomene:
- Custom `authenticationEntryPoint` vraća **401 JSON** (ne 403) za neautentifikovane zahteve. Frontend `errorInterceptor` radi auto-logout na 401, ali prikazuje toast na 403.
- `GET /api/user/me` mora biti eksplicitno pre `GET /api/user/**` (koji je `permitAll`), inače anonimni korisnici dobijaju 403 od `@PreAuthorize` umesto 401 od entrypoint-a.
- `DELETE /api/admin/**` mora imati vodeći `/` u putanji.
- HTTP security headers su uključeni: `X-Frame-Options: DENY`, HSTS (1 godina + subdomeni), CSP (dozvoljava Google/Facebook SDK, Cloudinary, Material Icons CDN).
- `POST /api/auth/refresh` i `POST /api/auth/logout` su `permitAll`; `GET /api/auth/ws-token` je `authenticated`.

**Phone number enkripcija**: `PhoneNumberConverter.java` (`security/`) — JPA `AttributeConverter` sa AES-256/CBC/PKCS5Padding. Random IV se prepend-uje svakom enkriptovanom vrednosti (Base64 encoded). Backward-compatible: ako dekriptovanje ne uspe, vraća raw vrednost (za postojeće plain-text brojeve u bazi). Ključ se čita iz `encryption.phone-key` property. Spring-managed komponenta (da bi `@Value` injection radilo u JPA konverteru — statičko polje sa setter injektovanjem).

### DTO Sigurnost (šta se NE šalje prema frontu)

- `UserShortDto` — sadrži `phoneNumber` ali **maskiran** kao `"06x / xxx-xxxx"` (null ako nema) — frontend koristi to samo da zna treba li prikazati dugme "Prikaži broj"; pravi broj se dohvata zasebnim `GET /api/user/{id}/phone` (requires auth)
- `AdDto` — nema `email` vlasnika (PII curenje)
- `RentalContractDto` — koristi `ContractParticipantDto` (samo id, ime, avatar) za lessee/owner, ne pun `UserDto`

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

### Material Icons

Dva fonta su učitana u `index.html`:
- **Material Icons** (klasični, popunjeni): `class="material-icons"` — CDN `family=Material+Icons`
- **Material Icons Outlined**: `class="material-icons-outlined"` — CDN `family=Material+Icons+Outlined`
- **Material Symbols Outlined** (noviji, tanji, varijabilni): `class="material-symbols-outlined"` — CDN sa osama `opsz,wght,FILL,GRAD`

Globalni CSS u `styles.css` za Symbols:
```css
.material-symbols-outlined {
  font-variation-settings: 'FILL' 0, 'wght' 100, 'GRAD' 0, 'opsz' 24;
}
```
Symbols imaju više ikona od klasičnih Icons (npr. `tools_power_drill`). Koristiti `material-symbols-outlined` za fine/tanke ikone, `material-icons` za standardne.

### App Shell Layout (`app.component`)

`app.component.css` uses flexbox with `gap: 185px` between sidebar and content. Key CSS classes:
- `.has-sidebar` — applied when sidebar is visible; used to apply max-width centering on pages without sidebar
- `.is-admin` — applied on `/admin` routes; removes padding from page-content

`app.component.ts` exposes:
- `showSidebar$` — based on **route only** (not auth state); `true` on all non-`/admin` routes
- `isAdmin$` — `true` when URL starts with `/admin`

Router uses `withPreloading(PreloadAllModules)` + `withInMemoryScrolling({ scrollPositionRestoration: 'top' })`.

### Sidebar (`core/layout/sidebar/`)

- Uses **Material Icons** (`<span class="material-icons nav-icon">`) — CDN loaded in `index.html`
- Color theme: purple `#813181` for avatar background, active state, hover state, and left border on active link
- Active link: `background: #f5ecff`, `border-left: 3px solid #813181`, `color: #813181`
- Unread badge (red `#e53935`) shown on Poruke and Obaveštenja links
- **Sidebar always visible** — shown on all non-admin routes regardless of login state
- **Guest state**: shows login/register buttons + locked (greyed-out, `pointer-events: none`) nav items
- **Logged-in state**: shows user avatar (initials), full interactive nav
- `SidebarComponent.ngOnInit()` calls both `NotificationService.initialize()` (chat unread) and `NotificationsService.loadUnreadCount()` (app notifications unread) — only when user is logged in

### Authentication Flow

**HttpOnly Cookie JWT** (XSS-safe — nema localStorage):
- Login vraća **dva HttpOnly cookie-ja**: `access_token` (15 min) + `refresh_token` (7 dana). Browser ih automatski šalje na svaki zahtev; JavaScript ih ne može pročitati.
- Login odgovor JSON sadrži i `wsToken` (kratkotrajan JWT) — čuva se samo **in-memory** u `AuthService.wsToken` (ne localStorage!) za STOMP `Authorization` header.
- `JwtFilter` čita `access_token` cookie **prvo**; fallback na `Authorization: Bearer` header (za WebSocket STOMP handshake koji ne može da šalje cookie-je).
- `AuthController` (`/api/auth/`): `POST /refresh` — validira `refresh_token` cookie, izdaje novi `access_token` cookie + vraća `{wsToken}`; `POST /logout` — briše oba cookie-ja (maxAge=0); `GET /ws-token` — vraća svež `wsToken` za WebSocket (requires auth).
- `app.cookie.secure=false` lokalno (HTTP); mora biti `true` u produkciji (`application-prod.properties`).

**Frontend token refresh flow**: `errorInterceptor` hvata 401 → ako nije auth endpoint → poziva `POST /api/auth/refresh` (refresh cookie se automatski šalje) → ponavlja originalni zahtev. Ako refresh ne uspe → redirect na `/login`.

`AuthService.loadInitialUser()` — na startu app poziva `GET /api/user/me` (cookie se šalje automatski). Ako uspe → dohvata i `wsToken` sa `GET /api/auth/ws-token`. Na 401 → ne radi ništa (korisnik nije ulogovan).

`authGuard` koristi `authService.currentUserValue` (ne localStorage).

**Angular dev proxy** (`proxy.conf.json` u Angular project root): proxira `/api` → `localhost:8080` i `/ws` → `localhost:8080` (WebSocket). Ovo čini da browser vidi sve sa `localhost:4200` → cookie-ji su same-origin u razvoju. Proxy je aktivan samo u `serve:development` konfiguraciji (`angular.json`).

Social login: Google (GIS button u `ngAfterViewInit`), Facebook (FB SDK), Apple (identity token). Svi koriste isti backend flow: verifikacija tokena → pronađi ili kreiraj korisnika → postavi HttpOnly cookie-je + vrati `{user, wsToken}`.

### Real-Time Chat

WebSocket endpoint at `/ws` (STOMP protocol). Frontend uses `RxStompService` (configured in `core/config/`).

`InboxComponent` (`features/chat/pages/inbox/`) uses `isLoadingMessages` flag — shows a spinner while messages load on conversation switch. Polling every 5s refreshes conversation list and active messages as WebSocket fallback.

### Message Types

`Message` entity has a `messageType` column (`VARCHAR(20)`, default `REGULAR`). Two values:
- `REGULAR` — normal user message
- `SYSTEM` — auto-generated by backend (e.g. contract accepted/rejected); rendered as centered italic gray bubble

### Global Chat Unread Badge

`NotificationService` (`core/services/`) holds a `BehaviorSubject<number>` for total unread chat message count:
- `initialize()` — fetches `GET /api/chat/unread-count`; called by `SidebarComponent.ngOnInit()`
- `updateFromConversations()` — recomputes from conversation list
- `onConversationOpened(n)` — optimistically subtracts `n` when conversation opened
- `onNewMessageInBackground()` — increments by 1 when WebSocket message arrives for background conversation

Backend: `MessageRepository.countUnreadForUser(userId)` uses single JPQL query joining `participantOne`/`participantTwo` to avoid N+1.

### In-App Notifications System

- `NotificationType` enum: `CONTRACT_REQUESTED`, `CONTRACT_ACCEPTED`, `CONTRACT_REJECTED`, `CONTRACT_CANCELLED`, `CONTRACT_ACTIVE`, `CONTRACT_FINISHED`, `NEW_REVIEW`, `AD_SAVED`
- `NotificationController` at `/api/notifications`: GET all, GET unread-count, PATCH read, PATCH read-all
- `NotificationsService` (`features/notifications/services/`) — `unreadCount$` BehaviorSubject
- Liquibase migration: `db.changelog-13-create-notification.xml`

### Save Count na oglasima

`Ad` entity ima `saveCount` kolonu (INT, default 0) — inkrementira u `AdServiceImpl.saveAd()`, dekrementira u `unsaveAd()` (minimum 0). Prikazuje se na `AdCardComponent` pored `viewCount` (ikonica `bookmark`).

### RentalCalendarComponent

Standalone component at `features/ads/components/rental-calendar/`. Accepts:
- `@Input() ad: Ad`
- `@Input() set blockedIntervals(value)` — setter that regenerates calendar when new intervals arrive
- `@Input() isMyAd: boolean` — shows "Block dates" button instead of "Send request" when true

Reused in both `AdDetailsComponent` and `InboxComponent` (third column).

### Ad List stranica i pretraga

`features/ads/pages/ad-list/` — glavna stranica sa oglasima. Tri moda:

**Home mod** (`homeMode = true`) — nema query params:
- `CategoriesSidebar` levo; desno: **Najnoviji oglasi** (9) + 5 fiksnih kategorija (6 svaka)
- 5 kategorija: Tehnologija i uređaji (ID 200), Oprema za film i fotografiju (300), Alati i oruđa (100), Događaji i zurke (600), Prevoz i oprema za prirodu (700)
- `loadHomeData()` radi 6 paralelnih API poziva; sve HTTP subscription-ovi imaju `takeUntil(destroy$)` da sprečavaju memory leak
- **Skeleton loaders**: `latestLoaded = false` flag + `readonly skeleton9 = Array(9)` / `readonly skeleton6 = Array(6)` — `SkeletonCardComponent` shimmer placeholders se prikazuju dok podaci ne stignu; `latestLoaded = true` se postavlja tek kad latest ads API odgovori

**Search mod** (`isSearchMode = true`) — aktivan kad ima keyword, categoryId, locationId, city, minPrice, maxPrice, priceInterval ILI sort param:
- `FiltersSidebar` levo, list view desno, loading spinner tokom pretrage
- Sortiranje dropdown (Najnovije / Najjeftinije / Najskuplje)

**Kritična arhitekturna napomena**: `adsPage` je `Page<AdPreview> | null` (NE Observable). `route.queryParams` subscription je **uvek aktivan** (nije unutar `*ngIf`) — to je ključno jer `async` pipe unutar `*ngIf` bi se unsubscribovao kad je `homeMode=true`, što bi sprečilo detekciju prelaza u search mod bez F5.

**NG0100 prevencija**: `isSearchMode` i `homeMode` se inicijalizuju sinhrono iz `route.snapshot` na startu `ngOnInit` pre prvog CD ciklusa.

Paginacija: numerisana, `…` za preskočene opsege, purple `#813181` za aktivnu stranicu.

### FiltersSidebar

`features/ads/components/filters-sidebar/`:
- **`previewCount`** — interno se računa debounced (350ms) API pozivom pri svakoj promeni filtera; prikazuje se u badge-u na "Prikaži oglase" dugmetu pre klika
- **`@Input() set initialCriteria`** — popunjava forme iz URL query params kad korisnik direktno pristupa search URL-u
- **`@Input() set locations`** — setter koji triggeruje `trySyncCityPicker()` za sinhronizaciju lokacije iz URL-a
- `onApplyFilters()` u parent-u eksplicitno postavlja SVE param-e (null briše iz URL-a) — bez `queryParamsHandling: 'merge'` jer bi stari param-i ostali

### Create/Edit Ad Wizard

Create Ad (`features/ads/pages/create-ad/`) i Edit Ad (`features/ads/pages/edit-ad/`):
- Step 1: Kategorija + naslov (char counter) + opis (char counter)
- Step 2: Drag-drop slike (10 max, 10MB), cena/valuta/interval, lokacija autocomplete, quantity stepper
- CSS: `margin-left: -200px; width: calc(100% + 200px)` da cancela app layout gap (reset na 900px)
- Kategorije koriste `material-symbols-outlined` sa `font-variation-settings` za thin stil

### My Ads stranica

`features/user/pages/my-ads/` — pretraga po naslovu (client-side), delete modal sa razlozima, Material Icons.

### Ad Details stranica

`features/ads/pages/ad-details/`:
- `latestReviews$` se subscribuje **jednom** sa `async` pipe u `*ngIf` — unutrašnji div koristi isti `reviews` template varijablu (ne `latestReviews$ | async` drugi put)
- Telefon vlasnika: dugme "Prikaži broj" vidljivo ako `ad.owner.phoneNumber` nije null (maskirana vrednost), pravi broj se dohvata klikom na zasebnom endpoint-u
- Za sopstveni oglas (`isMyAd=true`): prikazuje se "Izmeni" dugme sa RouterLink na `/ads/{id}/edit`

### Contracts stranica

`features/user/pages/contracts/` — dolazni i odlazni ugovori, pretraga po naslovu (client-side).

### Review Card

`features/review/components/review-card/` — ime recenzenta je klikabilni `<a>` link (`[routerLink]="['/user', review.reviewer.id]"`) koji vodi na profil korisnika. Fallback ime: `'Korisnik'` (ne placeholder sa pravim imenom).

### Color Theme

Two primary colors:
- **Purple** `#813181` — sidebar avatar, active nav links, notification badges/pills, buttons, accents
- **Green** `#6ecf7e` — secondary accent; opacity is intentionally varied per context

**Do not replace either color with blue or other colors.**

## Production

Aplikacija je deployovana na: **https://izdajemiznajmljujem.com**

- **VPS**: Hetzner CX22 — IP `178.104.97.101`, Ubuntu 22.04
- **Stack**: Docker Compose (`docker-compose.prod.yml`) — MySQL + backend + frontend + Nginx
- **SSL**: Let's Encrypt (Certbot), auto-renewal cron u 3h svake noći (`/opt/app/renew-ssl.sh`)
- **Slike**: Cloudinary (cloud name: `drwxucq4m`)
- **Mail**: Gmail SMTP (`izdajemiznajmljujem.rs@gmail.com`)
- **Repo na VPS-u**: `/opt/app/`
- **Env fajl**: `/opt/app/RentRentOut/.env` (nikad ne commitovati)
- **Symlink**: `/opt/app/.env` → `/opt/app/RentRentOut/.env` (potreban za Docker Compose varijable)

### Deploy komande (na VPS-u)

```bash
cd /opt/app
git pull
docker compose -f docker-compose.prod.yml up --build -d
```

### Produkciski profil

Backend koristi `--spring.profiles.active=prod` → učitava `application-prod.properties`.
`application.properties` je u `.gitignore` — ne postoji na serveru, sve ide kroz `application-prod.properties`.

### Ključne arhitekturne odluke

- `WebConfig.java` čita `app.frontend.base-url` iz properties za CORS (ne hardkodovano)
- Angular `environment.prod.ts` koristi relativni `/api` URL — Nginx proxira na backend
- WebSocket URL se izvodi iz `window.location` u produkciji (HTTP→WS, HTTPS→WSS)
- `google.client-id` je u `environment.ts` / `environment.prod.ts`, ne hardkodovan

### Šta treba ručno dodati na serveru (nije u gitu)

U `.env` fajlu na serveru (`/opt/app/RentRentOut/.env`):
```
APP_COOKIE_SECURE=true
ENCRYPTION_PHONE_KEY=<32-char-random-key>   ← POSTAVLJENO 2026-03-27
```

**Generisanje ključa na serveru**: `python3 -c "import secrets, string; print(''.join(secrets.choice(string.ascii_letters + string.digits) for _ in range(32)))"`

**VAŽNO**: `APP.COOKIE.SECURE` sa tačkama je **nevalidan** env var naziv na Linux/Docker-u. Koristiti `APP_COOKIE_SECURE` (sa underscoreom).

**Status (2026-03-27)**: `ENCRYPTION_PHONE_KEY` je postavljen na serveru. Baza podataka je očišćena — svi mock podaci obrisani, ostao samo pravi korisnik (id=10, `dimitrijemitic112@gmail.com`, role=ADMIN).

---

## Sistem Promocija (Monetizacija)

Prihod platforme dolazi od korisnika koji plaćaju promociju svojih oglasa. **Nema P2P plaćanja** — korisnici se međusobno dogovaraju za plaćanje.

### Model

- Svaki oglas traje **30 dana** od kreiranja (`expires_at` na `Ad` entity). Besplatno obnavljanje uvek (`POST /api/promotions/renew/{adId}`).
- Oglas po isteku dobija `adStatus = ARCHIVED` (scheduled job svakog jutra u 03:00).
- Kredit korisnika: `User.credit` (BigDecimal, već postoji). Admin dodaje kredit kroz admin panel.

### Paketi promocija (PromotionType enum)

| Tip | displayName | Cena | Trajanje | Efekt |
|---|---|---|---|---|
| `FEATURED` | Na vrhu | 500 RSD | 7 dana | `promotionRank=3` — uvek prvi u pretrazi |
| `PRIORITY` | Prioritetni | 250 RSD | 3 dana | `promotionRank=2` — ispred standardnih |
| `HIGHLIGHTED` | Istaknut oglas | 100 RSD | 30 dana | `promotionRank=0` — samo vizuelno (boja kartice) |

### Backend arhitektura (implementirano)

**Novi fajlovi:**
- `entity/Enums/PromotionType.java` — enum sa rank/price/duration
- `entity/Enums/TransactionType.java` — TOPUP_ADMIN, PROMOTION_PURCHASE, ADMIN_ADJUSTMENT
- `entity/AdPromotion.java` — istorija aktiviranih promocija (tabela `ad_promotion`)
- `entity/CreditTransaction.java` — istorija transakcija (tabela `credit_transaction`)
- `repository/AdPromotionRepository.java`
- `repository/CreditTransactionRepository.java`
- `dto/promotion/` — PromotionPackageDto, ActivatePromotionRequest, ActivePromotionDto, CreditBalanceDto, CreditTransactionDto
- `service/PromotionService.java` + `impl/PromotionServiceImpl.java`
- `controller/PromotionController.java`

**Modifikacije:**
- `entity/Ad.java` — dodato: `expiresAt`, `promotionType`, `promotionExpiresAt`, `promotionRank` (int, default 0)
- `dto/ad/AdPreviewDto.java` — dodato: `expiresAt`, `promotionType`
- `mapper/AdMapper.java` — mapira nova polja u `toPreviewDto()`
- `service/impl/AdServiceImpl.java` — `withPromotionSort()` metoda prependa `promotionRank DESC` sort
- `security/SecurityConfig.java` — dodati `/api/promotions/**` endpointi
- `db/changelog/db.changelog-master.xml` — include 21 i 22

**Migracije:**
- `db.changelog-21-add-ad-expiry-and-promotion.xml` — `expires_at`, `promotion_type`, `promotion_expires_at`, `promotion_rank` na `ad` tabeli
- `db.changelog-22-create-promotion-system.xml` — `ad_promotion` i `credit_transaction` tabele

**Scheduled jobs (u PromotionServiceImpl):**
- `expirePromotions()` — svakih sat (fixedDelay=3600000): resetuje `promotionType/promotionRank` na oglasima čija promocija je istekla
- `expireAds()` — svako jutro u 03:00 (cron): postavlja `adStatus=ARCHIVED` za istekle oglase

### API Endpointi

| Method | URL | Auth | Opis |
|---|---|---|---|
| GET | `/api/promotions/packages` | public | Lista paketa sa cenama |
| POST | `/api/promotions/activate` | auth | Aktivira promociju (skida kredit) |
| GET | `/api/promotions/ad/{adId}` | public | Aktivne promocije za oglas |
| POST | `/api/promotions/renew/{adId}` | auth | Besplatna obnova oglasa |
| GET | `/api/promotions/credit` | auth | Stanje kredita korisnika |
| GET | `/api/promotions/credit/history` | auth | Istorija transakcija |
| POST | `/api/promotions/admin/credit` | admin | Dodaj kredit korisniku |

### Frontend (IMPLEMENTIRANO)

Svi frontend delovi sistema promocija su implementirani:

1. **`AdPreview` model** (`shared/models/adPreview.model.ts`) — dodata `expiresAt?: string` i `promotionType?: PromotionType | null`; `PromotionType = 'FEATURED' | 'PRIORITY' | 'HIGHLIGHTED'` eksportovan odavde
2. **`PromotionService`** (`features/ads/services/promotion.service.ts`) — `getPackages()`, `activate()`, `getActivePromotions()`, `renewAd()`, `getCreditBalance()`, `getCreditHistory()`, `addCredit()` (admin)
3. **`AdCardComponent`** — badge `<span class="promo-badge badge-featured/priority/highlighted">` dodat na `.image-wrapper`, pozicioniran `top:8px; left:8px`
4. **"Moji oglasi"** (`features/user/pages/my-ads/`) — `ad-management-actions` prikazuje: expiry info (crveno/narandžasto kad ≤5 dana), promo badge ako aktivan, dugmad "Obnovi" (besplatno, blokira dupli klik) + "Promoviši" (otvara modal) + Izmeni + Obriši
5. **`PromotionModalComponent`** (`features/ads/components/promotion-modal/`) — modalni prozor sa 3 paketa, stanjem kredita, disabled paket ako nema dovoljno kredita, link "Dopuni" ka `/credit`
6. **Stranica "Kredit"** (`features/user/pages/credit/`) — ruta `/credit` (auth guard), prikazuje: balance card, uputstvo za dopunu, tabelu paketa, istoriju transakcija
7. **Admin panel** (`features/admin/pages/admin-users/`) — kolona "Kredit" sa dugmetom "+ Kredit"; modal sa iznosom i napomenom; poziva `POST /api/promotions/admin/credit`

**Stil za promotion badge:**
```css
.badge-featured  { background: linear-gradient(135deg, #f59e0b, #d97706); color: #fff; }
.badge-priority  { background: var(--color-primary); color: #fff; }
.badge-highlighted { background: var(--color-primary-light); color: var(--color-primary); border: 1px solid var(--color-primary-border); }
```

### Cookie Banner + Legal Stranice (IMPLEMENTIRANO)

- **`CookieConsentService`** (`shared/services/cookie-consent.service.ts`) — `localStorage` key `cookie_consent`; GA4 se učitava dinamički samo na prihvat; **GA4 ID: `G-GYYJSDLKLB`** (postavljeno, aktivan)
- **`CookieBannerComponent`** (`shared/components/cookie-banner/`) — fixed bottom banner, `*ngIf="(status$ | async) === null"`, slide-up animacija
- **`/privacy-policy`** — potpun ZZPL/GDPR tekst na srpskom (10 sekcija)
- **`/terms-of-service`** — potpun tekst uslova korišćenja (9 sekcija), uključuje opis kredit sistema i cene paketa
- Footer linkovi ažurirani na `/privacy-policy` i `/terms-of-service`

### Sentry (IMPLEMENTIRANO)

Backend: `sentry-spring-boot-starter-jakarta`. Frontend: `@sentry/angular^10.46.0`, `Sentry.init()` pre `bootstrapApplication()` u `main.ts`.

Za produkciju, dodati u `/opt/app/RentRentOut/.env`:
```
SENTRY_DSN=https://...@sentry.io/...
SENTRY_TRACES_SAMPLE_RATE=0.1
SENTRY_ENVIRONMENT=production
```

### Google Analytics (IMPLEMENTIRANO)

GA4 Measurement ID je postavljen u `shared/services/cookie-consent.service.ts`. Skript se učitava dinamički samo kad korisnik prihvati kolačiće.

### SEO Meta Tagovi (IMPLEMENTIRANO)

- **`index.html`** — statički default tagovi: `description`, `og:description`, `og:image`, `og:url`, `twitter:card/title/description/image`
- **`AdDetailsComponent`** — dinamički tagovi putem Angular `Title` i `Meta` servisa:
  - `<title>` → `{naslov oglasa} — Izdajem Iznajmljujem`
  - `og:title`, `og:description` (prvih 155 znakova opisa), `og:image` (prva slika), `og:url`, `og:type: product`
  - `twitter:card/title/description/image`
  - `ngOnDestroy()` resetuje tagove na default vrednosti
- **Napomena**: Angular je SPA — Google Crawler izvršava JS i vidi dinamičke tagove. Za Facebook/WhatsApp deljenje koristi se Cloudinary URL slike koji je javno dostupan. Za optimalni crawling bez JS (stariji botovi) potreban bi bio Angular SSR — nije implementiran.

### Email Podsetnik za Istek Oglasa (IMPLEMENTIRANO)

- **`AdRepository.findAdsExpiringBetween(from, to)`** — JPQL query za aktivne oglase čiji `expiresAt` pada u zadati vremenski prozor
- **`PromotionServiceImpl.sendExpiryReminders()`** — `@Scheduled(cron = "0 0 10 * * *")`, svako jutro u 10:00; prozor `[now+2d, now+3d]` hvata svaki oglas tačno jednom; email sadrži link na oglas i link na Moji oglasi za obnovu; greška na jednom emailu ne prekida ostale

---

## HTML Email Servis (IMPLEMENTIRANO)

`HtmlEmailServiceImpl.java` (`service/impl/`) — zamena za `SimpleMailMessage`/`JavaMailSender` u svim service-ima.

- Purple-themed table-based HTML layout (kompatibilan sa svim email klijentima)
- `esc()` helper za XSS zaštitu svih user-content vrednosti
- 7 implementiranih metoda: `sendVerificationEmail`, `sendPasswordResetEmail`, `sendContractRequestEmail`, `sendContractAcceptedEmail`, `sendContractRejectedEmail`, `sendCreditAddedEmail`, `sendAdExpiryReminderEmail`
- `send()` hvata sve exceptione i loguje warning — ne blokira main flow
- Svi emaili na **srpskom jeziku** (lokalizovano u ovoj sesiji)

**Refaktorisani servisi:**
- `EmailVerificationServiceImpl` — koristi `HtmlEmailService`; interfejs sada prima `firstname` parametar
- `PasswordResetServiceImpl` — koristi `HtmlEmailService`
- `NotificationServiceImpl` — sve 3 contract email metode; push notifikacije naslovi na srpskom
- `PromotionServiceImpl` — `addCredit()` i `sendExpiryReminders()` emaili

---

## Ad Reporting System (IMPLEMENTIRANO)

Korisnici mogu prijaviti oglase. Admin vidi prijave u posebnom tabu.

### Backend

- **`AdReport.java`** (entity) — `id`, `ad` (lazy FK), `reporter` (lazy FK), `reason` (VARCHAR 60), `note` (VARCHAR 500), `reviewed` (boolean), `createdAt`
- **`AdReportRepository.java`** — `existsByAdIdAndReporterId()`, `findAllByOrderByCreatedAtDesc()`, `findAllByReviewedFalseOrderByCreatedAtDesc()`, `countByReviewedFalse()`
- **`dto/admin/AdReportDto.java`** — static `from(AdReport r)` factory
- **`db.changelog-24-create-ad-report.xml`** — `ad_report` tabela sa FK cascade delete i indexima
- **`AdReportController`** — `POST /api/ads/{adId}/report` (authenticated; guard: ne može sopstveni oglas; duplikat guard)
- **`AdminController`** — `GET /api/admin/reports?onlyUnreviewed=true|false`, `PATCH /api/admin/reports/{id}/reviewed`
- **`AdminServiceImpl`** — `getStats()` sada vraća 6 polja: dodato `activeAds` i `pendingReports`; `getReports()`, `markReportReviewed()`
- **`AdRepository`** — dodato `findAllActiveIds()` JPQL query (koristi ga i `SitemapController`)

### Frontend

- **`ReportModalComponent`** (`features/ads/components/report-modal/`) — 5 razloga + opcionalna napomena (500 char); `ToastService.showSuccess/showError`
- **`AdDetailsComponent`** — dugme "Prijavi oglas" vidljivo samo ulogovanim ne-vlasnicima; `reportOpen: boolean`
- **`ad.service.ts`** — dodato `reportAd(adId, reason, note): Observable<string>`
- **`AdminReportsComponent`** (`features/admin/pages/admin-reports/`) — tabela sa filterom (samo nepregledane / sve), "Označi pregledano", paginacija
- **`admin.routes.ts`** — `{ path: 'reports', component: AdminReportsComponent }`
- **`admin-shell.component.html`** — nav link "Prijave"
- **`admin-dashboard.component.html`** — 6 stat kartica (Korisnici, Ukupno oglasi, Aktivni oglasi, Ugovori, Aktivni ugovori, Nepregledane prijave — crveno kad > 0)
- **`admin.service.ts`** — `AdminStats` prošireno sa `activeAds`, `pendingReports`; dodato `getReports()`, `markReportReviewed()`

---

## Sitemap.xml (IMPLEMENTIRANO)

- **`SitemapController.java`** — `GET /sitemap.xml`, `MediaType.APPLICATION_XML_VALUE`; statičke stranice + sve aktivne oglasi iz `AdRepository.findAllActiveIds()`
- **`nginx.prod.conf`** — `location = /sitemap.xml` proxira na `backend:8080` (pre `/api/` bloka)
- **`public/robots.txt`** — `Allow: /`, `Sitemap: https://izdajemiznajmljujem.com/sitemap.xml`
- **`SecurityConfig`** — `GET /sitemap.xml` je `permitAll`

---

## PWA i Performance (IMPLEMENTIRANO)

- **`public/manifest.webmanifest`** — PWA manifest, `theme_color: "#813181"`, `display: "standalone"`, ikone
- **`index.html`** — `<meta name="theme-color" content="#813181">` + `<link rel="manifest" href="manifest.webmanifest">`
- **`ad-card.component.html`** — `loading="lazy"` na `<img>` tagu
- **`angular.json`** — `anyComponentStyle` budget: `maximumWarning` 8kB→16kB, `maximumError` 16kB→24kB (pre-existing overrun u create-ad/edit-ad komponentama)

---

## DB Backup Script (IMPLEMENTIRANO)

**`backup.sh`** (u project root, deployuje se na VPS):
- MySQL dump via `docker exec mysql`, gzip kompresija
- 14-dnevna rotacija (automatsko brisanje starih backup-ova)
- Cron na serveru: `0 2 * * * /opt/app/backup.sh >> /opt/app/backups/backup.log 2>&1`

---

## Rate Limiting (VEĆ IMPLEMENTIRANO)

`RateLimitFilter.java` (`security/`) — `@Component`, Bucket4j biblioteka. Štiti:
- `POST /api/auth/login`, `POST /api/auth/register`, `POST /api/auth/forgot-password`
- Social login endpointe
Auto-registrovan kao Spring servlet filter — bez promene `SecurityConfig`.

---

## Produkcijska Baza Podataka (STATUS 2026-03-27)

- Mock podaci potpuno obrisani (`TRUNCATE` svih data tabela: ad, ad_image, ad_view, review, notification, message, conversation, rental_contract, saved_ad, ad_report, ad_promotion, credit_transaction, email_verification_token, password_reset_token)
- Jedini preostali korisnik: **id=10**, `dimitrijemitic112@gmail.com`, `role_id=1` (ADMIN)
- `ENCRYPTION_PHONE_KEY` postavljen u `/opt/app/RentRentOut/.env`

**Napomena za MySQL Docker konekciju**: koristiti `-h 127.0.0.1` (TCP), ne `localhost` (socket fail sa root passwordom):
```bash
docker exec -it <mysql-container> mysql -h 127.0.0.1 -u root -p
```

---

## Communication

Uvek odgovaraj na srpskom jeziku (latinica). Korisnik je iz Srbije.
