# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Full-stack rental marketplace — **https://izdajemiznajmljujem.com**
- **Backend**: Spring Boot 3.2.4 (Java 17, Maven) — REST API + WebSocket
- **Frontend**: Angular 19.2 (TypeScript) — SPA, lazy-loaded feature modules
- **Database**: MySQL 8.0 + Liquibase migrations

## Development Commands

```bash
# Frontend (RentRentOutFront/rent-rent-out-front/)
npm start          # Dev server :4200
npm run build      # Production build
npm test           # Karma + Jasmine
ng test --include='**/foo.spec.ts'

# Backend (RentRentOut/)
mvn spring-boot:run
mvn package -DskipTests
mvn test
mvn test -Dtest=FooTest

# Full Stack
docker-compose up --build
```

## Git Worktrees

- `C:/xampp/htdocs/Rent Rent Out/` — `main`
- `C:/xampp/htdocs/RentRentOut-Profile/` — `features/user-profile`

## Architecture

### Backend (`RentRentOut/src/main/java/org/landm/`)

Layered: Controller → Service (interface + impl) → Repository (JPA) → Entity

- `controller/` — REST endpoints; `ChatWsController` (WebSocket); `AuthController` (`/api/auth/refresh|logout|ws-token`)
- `service/` — Business logic
- `entity/` — `User`, `Ad`, `RentalContract`, `Review`, `Conversation`, `Message`, `Notification`, `AdReport`, `AdPromotion`, `CreditTransaction`
- `dto/` — DTOs grouped by feature
- `repository/` — Spring Data JPA
- `security/` — `JwtUtil`, `JwtFilter`, `JwtChannelInterceptor`, `SecurityConfig`, `PhoneNumberConverter` (AES-256)
- `config/` — `WebConfig` (CORS), `WebSocketConfig`, mail

**Liquibase**: migrations in `src/main/resources/db/changelog/`. Never edit existing changesets — always add new numbered XML files. Current: 1–24.

**Spring profiles**: `application.properties` (local), `application-docker.properties`, `application-prod.properties`.

**Explicit pom.xml versions** (not managed by Spring Boot BOM):
- `com.nimbusds:nimbus-jose-jwt:9.37.3` — Apple JWT verification
- `com.google.api-client:google-api-client:2.2.0` — Google OAuth

### Security

`SecurityConfig.java` — ključne napomene:
- Custom `authenticationEntryPoint` vraća **401 JSON** (ne 403). Frontend `errorInterceptor` radi auto-logout na 401, prikazuje toast na 403.
- `GET /api/user/me` mora biti pre `GET /api/user/**` (koji je `permitAll`) — inače anonimni korisnici dobijaju 403 od `@PreAuthorize`.
- `DELETE /api/admin/**` mora imati vodeći `/`.
- HTTP headers: `X-Frame-Options: DENY`, HSTS (1 god + subdomeni), CSP (Google/Facebook SDK, Cloudinary, Material Icons CDN).
- `GET /api/reviews/contract-with/**` mora biti `authenticated()` pre opšteg `GET /api/reviews` koji je `permitAll`.

**Phone encryption**: `PhoneNumberConverter` — AES-256/CBC/PKCS5Padding, random IV prepended (Base64). Backward-compatible. Key iz `encryption.phone-key`. Spring-managed sa statičkim poljem + setter injection za `@Value`.

**Rate limiting**: `RateLimitFilter` (`security/`) — Bucket4j, štiti login/register/forgot-password + social login. Auto-registrovan kao Spring servlet filter.

### DTO Security

- `UserShortDto.phoneNumber` — maskiran kao `"06x / xxx-xxxx"` (null ako nema); pravi broj: `GET /api/user/{id}/phone` (requires auth)
- `AdDto` — nema email vlasnika
- `RentalContractDto` — koristi `ContractParticipantDto` (id, ime, avatar), ne pun `UserDto`

### Frontend (`src/app/`)

- `core/` — `layout/` (Header, Navbar, Sidebar, Footer); `services/` (`NotificationService` — chat unread badge); `config/` (API endpoints, RxStomp config)
- `shared/` — TypeScript models, Toast, SkeletonCard, pipes, `CookieConsentService`
- `features/` — lazy-loaded:
  - `auth/` — Login, Register (ToS checkbox), email verify, password reset
  - `ads/` — listings, details, create/edit wizard, `RentalCalendarComponent`, `PromotionModalComponent`, `ReportModalComponent`
  - `chat/` — 3-column inbox (conversations | messages | calendar)
  - `user/` — `my-ads`, `saved-ads`, `contracts`, `my-profile`, `credit`
  - `review/` — rating form + review cards
  - `notifications/` — notification center
  - `legal/` — `/privacy-policy`, `/terms-of-service`, `/how-it-works`, `/contact`
  - `admin/` — dashboard, users, ads, contracts, reports, credits

### Material Icons

Tri fonta u `index.html`:
- `class="material-icons"` — popunjeni (standardni)
- `class="material-icons-outlined"` — outlined
- `class="material-symbols-outlined"` — tanki, varijabilni (više ikona, npr. `tools_power_drill`)

```css
/* styles.css */
.material-symbols-outlined { font-variation-settings: 'FILL' 0, 'wght' 100, 'GRAD' 0, 'opsz' 24; }
```

### App Shell

`app.component.css`: flexbox sa `gap: 185px` između sidebar i sadržaja.
- `.has-sidebar` — max-width centering na stranicama bez sidebar-a
- `.is-admin` — uklanja padding (admin rute)
- `showSidebar$` — baziran na ruti (ne auth state); `true` na svim non-`/admin` rutama
- Router: `withPreloading(PreloadAllModules)` + `withInMemoryScrolling({ scrollPositionRestoration: 'top' })`

### Sidebar (`core/layout/sidebar/`)

- Uvek vidljiv (non-admin rute) — guest state i logged-in state
- **Avatar**: `<img>` ako `user.avatarUrl` postoji, inače inicijali (purple `#813181` background)
- Active link: `background: #f5ecff`, `border-left: 3px solid #813181`, `color: #813181`
- Unread badge (red `#e53935`) na Poruke i Obaveštenja
- `ngOnInit()`: ako ulogovan → `NotificationService.initialize()` + `NotificationsService.loadUnreadCount()`
- Guest state: login/register dugmad + zaključani nav itemi (`pointer-events: none`)

### Authentication Flow

**HttpOnly Cookie JWT**:
- Login → dva cookie-ja: `access_token` (15 min) + `refresh_token` (7 dana) + JSON `{wsToken}` (in-memory, za STOMP header)
- `JwtFilter`: čita `access_token` cookie prvo, fallback na `Authorization: Bearer` header
- `errorInterceptor`: 401 → `POST /api/auth/refresh` → retry; ako ne uspe → `/login`
- `AuthService.loadInitialUser()`: `GET /api/user/me` na startu → ako uspe, dohvata `wsToken` sa `GET /api/auth/ws-token`
- Dev proxy (`proxy.conf.json`): `/api` i `/ws` → `localhost:8080`

Social login: Google (GIS), Facebook (FB SDK), Apple (identity token) → isti backend flow.

### Chat i Poruke

**Message types** (`MessageType` enum, `VARCHAR(20)`):
- `REGULAR` — obična poruka (bubble, desno/levo)
- `SYSTEM` — auto-generisana od backenda; renderuje se kao centrirani sivi kursiv bubble
- `CONTRACT_REQUEST` — kartica ugovora; ikone: `handshake`, `inventory_2`, `calendar_today`, `event`, `payments` (sve `material-icons`, ne emojiji)

`InboxComponent`: `isLoadingMessages` spinner pri promeni konverzacije; polling 5s kao WebSocket fallback.

**Chat unread badge** (`NotificationService`, `core/services/`):
- `BehaviorSubject<number>`, `initialize()` → `GET /api/chat/unread-count`
- `updateFromConversations()`, `onConversationOpened(n)`, `onNewMessageInBackground()`
- Backend: `MessageRepository.countUnreadForUser()` — JPQL join participantOne/Two (bez N+1)

### Notifications

- `NotificationType`: CONTRACT_REQUESTED/ACCEPTED/REJECTED/CANCELLED/ACTIVE/FINISHED, NEW_REVIEW, AD_SAVED
- `/api/notifications`: GET all, GET unread-count, PATCH read, PATCH read-all
- `NotificationsService` (`features/notifications/services/`) — `unreadCount$` BehaviorSubject
- Migracija: `db.changelog-13-create-notification.xml`

### Ad List & Search

`features/ads/pages/ad-list/` — dva moda:

**Home mod** (`homeMode = true`): hero banner na vrhu + `CategoriesSidebar` + Najnoviji (9) + 5 kategorija (ID: 200, 300, 100, 600, 700). `loadHomeData()` — 6 paralelnih API poziva, skeleton loaders (`latestLoaded` flag, `skeleton9/skeleton6`). Sve subscription-i imaju `takeUntil(destroy$)`.

**Search mod** (`isSearchMode = true`): `FiltersSidebar` + list view + sortiranje. Kritično: `route.queryParams` subscription **uvek aktivan** (ne unutar `*ngIf`) — inače se ne detektuje prelaz homeMode→searchMode bez F5. `isSearchMode`/`homeMode` inicijalizuju se sinhrono iz `route.snapshot` (NG0100 prevencija).

Paginacija: numerisana, `…`, purple aktivna stranica.

**FiltersSidebar**: debounced (350ms) `previewCount`; `@Input() set initialCriteria` (iz URL params); `onApplyFilters()` eksplicitno postavlja SVE params (bez `queryParamsHandling: 'merge'`).

### Create/Edit Ad Wizard

- Step 1: kategorija + naslov (char counter) + opis
- Step 2: drag-drop slike (10 max, 10MB), cena/valuta/interval, lokacija autocomplete, quantity stepper
- CSS: `margin-left: -200px; width: calc(100% + 200px)` (cancela sidebar gap, reset na 900px)
- Kategorije: `material-symbols-outlined` sa `font-variation-settings`
- `angular.json` style budget: `maximumWarning` 16kB, `maximumError` 24kB

### Ad Details (`features/ads/pages/ad-details/`)

- `latestReviews$` — subscribuje se jednom sa `async` pipe u `*ngIf` (unutrašnji div reuses `reviews` varijablu)
- Telefon: dugme "Prikaži broj" vidljivo ako `ad.owner.phoneNumber` nije null; broj se dohvata klikom
- `isMyAd=true`: prikazuje "Izmeni" dugme, sakriva "Pošalji zahtev" i "Prijavi oglas"
- `isMyAd=false` + ulogovan: prikazuje "Prijavi oglas" (otvara `ReportModalComponent`)
- Dynamic SEO: Angular `Title`+`Meta` servisi; `ngOnDestroy()` resetuje tagove

### Review System

`GET /api/reviews/contract-with/{userId}` (authenticated) — vraća `{ contractId: number | null }`:
- Pronalazi završene ugovore između currentUser i userId (`findFinishedBetweenUsers()`)
- Filtrira: `endDate >= now - 30 dana` i korisnik još nije ostavio recenziju za taj ugovor
- Koristi se u `UserProfileComponent` — "Ocenite" dugme vidljivo samo ako `reviewContractId !== null`
- `reviewCheckDone` flag sprečava prikaz dugmeta pre odgovora sa servera

`ReviewCard`: ime recenzenta je `<a [routerLink]="['/user', review.reviewer.id]">`; fallback: `'Korisnik'`.

### Register

- `termsAccepted: [false, Validators.requiredTrue]` u form grupi
- Getter: `get termsAccepted()` za template validaciju
- Submit payload: `const { termsAccepted: _, ...payload } = this.registerForm.value` (ne šalje se backendu)
- Link na `/terms-of-service` i `/privacy-policy` u checkboxu

### Legal Stranice (`features/legal/pages/`)

- `/privacy-policy` — ZZPL/GDPR tekst (10 sekcija)
- `/terms-of-service` — uslovi korišćenja (9 sekcija, cene paketa)
- `/how-it-works` — hero + dvostubački layout (vlasnik / iznajmljivač), napomene, CTA
- `/contact` — email podrška, GDPR kartica, prijava oglasa kartica, pravne info

Footer: samo stvarne rute (Platforma: how-it-works, terms, privacy, contact; Korisno: create, browse, register, login). Nema dead linkova.

### User Pages

- `my-ads/` — client-side pretraga, expiry info (crveno/narandžasto ≤5 dana), promo badge, dugmad: Obnovi + Promoviši + Izmeni + Obriši
- `contracts/` — dolazni i odlazni, pretraga po naslovu (client-side)
- `credit/` (`/credit`, auth guard, `max-width: 860px`) — balance card, uputstvo, tabela paketa, istorija transakcija
- `RentalCalendarComponent` (`features/ads/components/rental-calendar/`) — standalone, reused u AdDetails i InboxComponent; `@Input() isMyAd` preklapanje "Blokiraj" umesto "Pošalji zahtev"

### Color Theme

- **Purple** `#813181` — primary: buttons, avatar, active nav, badges, accents
- **Green** `#6ecf7e` — secondary accent

**Ne zamenjivati ni jednu boju plavom.**

---

## Sistem Promocija (Monetizacija)

Prihod od promocija oglasa. Nema P2P plaćanja.

**Paketi** (`PromotionType` enum):
| Tip | Cena | Trajanje | Efekt |
|---|---|---|---|
| `FEATURED` | 500 RSD | 7 dana | `promotionRank=3` — vrh pretrage |
| `PRIORITY` | 250 RSD | 3 dana | `promotionRank=2` — ispred standardnih |
| `HIGHLIGHTED` | 100 RSD | 30 dana | `promotionRank=0` — samo vizuelno |

**Promotion badge CSS:**
```css
.badge-featured    { background: linear-gradient(135deg, #f59e0b, #d97706); color: #fff; }
.badge-priority    { background: var(--color-primary); color: #fff; }
.badge-highlighted { background: var(--color-primary-light); color: var(--color-primary); border: 1px solid var(--color-primary-border); }
```

**Backend entiteti**: `AdPromotion`, `CreditTransaction`; enumi `PromotionType`, `TransactionType`.
**Ad entity**: `expiresAt`, `promotionType`, `promotionExpiresAt`, `promotionRank` (int, default 0).
**Migracije**: changelog-21 (expiry + promotion kolone), changelog-22 (tabele ad_promotion, credit_transaction).

**Scheduled jobs** (u `PromotionServiceImpl` — `@Slf4j`):
- `expirePromotions()` — svakih sat: resetuje promotionType/Rank na isteklim
- `expireAds()` — 03:00 cron: `adStatus=ARCHIVED` za istekle oglase
- `sendExpiryReminders()` — 10:00 cron: email za oglase koji ističu za 2–3 dana (prozor `[now+2d, now+3d]`)

Email u `addCredit()` i `sendExpiryReminders()` je wrapped u try-catch + `log.warn` — ne blokira main flow.

**API**: `GET /api/promotions/packages`, `POST /api/promotions/activate`, `GET /api/promotions/ad/{adId}`, `POST /api/promotions/renew/{adId}`, `GET /api/promotions/credit`, `GET /api/promotions/credit/history`, `POST /api/promotions/admin/credit`.

---

## HTML Email Servis

`HtmlEmailServiceImpl` (`service/impl/`) — purple table-based HTML, kompatibilan sa svim klijentima. `esc()` helper za XSS zaštitu. `send()` hvata sve exceptione. 7 metoda: verifikacija, reset lozinke, contract request/accepted/rejected, credit added, ad expiry reminder. Svi emaili na srpskom.

---

## Ad Reporting

**Backend**: `AdReport` entity (reason VARCHAR 60, note VARCHAR 500, reviewed bool); `POST /api/ads/{adId}/report` (ne može sopstveni oglas, duplikat guard); `GET /api/admin/reports?onlyUnreviewed=`, `PATCH /api/admin/reports/{id}/reviewed`. Migracija: changelog-24.

**Frontend**: `ReportModalComponent` (5 razloga + napomena 500 char); admin stranica sa filterom i paginacijom; dashboard prikazuje 6 stat kartica (uključuje activeAds i pendingReports — crveno kad > 0).

---

## SEO, Sitemap, PWA

- **Sitemap**: `SitemapController` → `GET /sitemap.xml` (permitAll); statičke stranice + svi aktivni oglasi iz `findAllActiveIds()`; Nginx proxira `/sitemap.xml` na backend
- **robots.txt**: `public/robots.txt` — `Allow: /`, sitemap URL
- **SEO**: `AdDetailsComponent` dynamički postavlja `<title>`, og:title/description/image/url/type, twitter:card; `ngOnDestroy()` resetuje na defaulte
- **PWA**: `manifest.webmanifest` (theme_color `#813181`, standalone), `<meta name="theme-color">`, `loading="lazy"` na ad kartici
- **GA4**: ID `G-GYYJSDLKLB`, učitava se dinamički samo na prihvat kolačića (`CookieConsentService`, localStorage key `cookie_consent`)
- **Cookie banner**: `CookieBannerComponent` (`shared/components/cookie-banner/`), `*ngIf="(status$ | async) === null"`, slide-up animacija
- **Sentry**: backend `sentry-spring-boot-starter-jakarta`; frontend `@sentry/angular^10`; `Sentry.init()` pre `bootstrapApplication()`

---

## Production

- **VPS**: Hetzner CX22 — `178.104.97.101`, Ubuntu 22.04
- **Stack**: Docker Compose (`docker-compose.prod.yml`) — MySQL + backend + frontend + Nginx
- **SSL**: Let's Encrypt, auto-renewal cron 03:00 (`/opt/app/renew-ssl.sh`)
- **Repo na VPS-u**: `/opt/app/`; **Env**: `/opt/app/RentRentOut/.env`; **Symlink**: `/opt/app/.env` → `/opt/app/RentRentOut/.env`
- **Backup**: `backup.sh` — MySQL dump gzip, 14-dnevna rotacija; cron `0 2 * * *`
- **Deploy**: `cd /opt/app && git pull && docker compose -f docker-compose.prod.yml up --build -d`
- Angular `environment.prod.ts` koristi relativni `/api` URL (Nginx proxira); WebSocket URL se izvodi iz `window.location`; `google.client-id` u `environment.ts` (ne hardkodovan)
- `app.cookie.secure=false` lokalno; `APP_COOKIE_SECURE=true` u `.env` na serveru

**Env varijable na serveru** (nisu u gitu):
```
APP_COOKIE_SECURE=true
ENCRYPTION_PHONE_KEY=<32 chars>   # postavljeno 2026-03-27
SENTRY_DSN=https://...@sentry.io/...
SENTRY_TRACES_SAMPLE_RATE=0.1
SENTRY_ENVIRONMENT=production
```
Generisanje ključa: `python3 -c "import secrets, string; print(''.join(secrets.choice(string.ascii_letters + string.digits) for _ in range(32)))"`

**VAŽNO**: `APP.COOKIE.SECURE` (sa tačkama) je nevalidan env var na Linux. Koristiti underscore.

**MySQL Docker konekcija**: `-h 127.0.0.1` (TCP), ne `localhost` (socket fail):
```bash
docker exec -it <mysql-container> mysql -h 127.0.0.1 -u root -p
```

**DB status (2026-03-27)**: Mock podaci obrisani. Jedini korisnik: id=10, `dimitrijemitic112@gmail.com`, role=ADMIN.

---

## Communication

Uvek odgovaraj na srpskom jeziku (latinica). Korisnik je iz Srbije.
