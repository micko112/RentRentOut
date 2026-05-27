# IzdajemIznajmljujem Wiki

Dobrodošao u dokumentaciju projekta. Glavni README je [ovde](../README.md); ova Wiki služi kao **detaljna referenca** po komponentama.

<p align="center">
  <img src="../docs/screenshots/hero-banner.png" width="80%" alt="banner" />
</p>

## Brza navigacija

### 🏛 Arhitektura
- [Architecture](Architecture.md) — visok nivo, request flow, dijagrami
- [Database Schema](Database-Schema.md) — entity-i, relacije, migracije

### 🔷 Backend (Spring Boot)
- [Backend](Backend.md) — package layout, slojevi, scheduleri, email
- [Authentication and Security](Authentication-and-Security.md) — JWT cookies, AES-256, rate limiting
- [API Reference](API-Reference.md) — REST + WebSocket endpoint-i

### 🔴 Frontend (Angular)
- [Frontend](Frontend.md) — feature moduli, RxJS pattern-i, layout breakpoint-i

### 🧠 AI / ML
- [ML Service](ML-Service.md) — PyTorch MLP za auto-suggest kategorije
- [Chatbot](Chatbot.md) — LangChain + Chroma RAG chatbot

### 💰 Domen
- [Promotion System](Promotion-System.md) — paketi, kredit, expiry job

### 🚢 DevOps
- [Deployment](Deployment.md) — VPS, Nginx, SSL, backup
- [Configuration](Configuration.md) — env varijable po servisu

### 📋 Specifikacija
- [Use Cases](Use-Cases.md) — 72 use case-a

---

## Konvencije u dokumentaciji

- **Code blocks** koriste sintaksne highlight-ove (`java`, `typescript`, `python`, `properties`, `bash`)
- **Linkovi** između Wiki stranica su relativni (`[Name](Page.md)`)
- **Screenshots** su u [`docs/screenshots/`](../docs/screenshots/README.md)
- Sve poruke u email-ovima i UI-u su **na srpskom (latinica)**

## Kontribucija dokumentaciji

1. Edituj odgovarajući `.md` u `wiki/`
2. Ako dodaješ novu stranicu, doda je u [Home.md](Home.md) i u glavni [README.md](../README.md) tabelu
3. Commit + push
4. (Opciono) sinhronizuj sa GitHub Wiki repoom (vidi README sekciju "Dokumentacija (Wiki)")
