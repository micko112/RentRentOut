# IzdajemIznajmljujem Wiki

Welcome to the project documentation. The main README is available [here](../README.md); this Wiki serves as a **detailed reference** organised by component.

<p align="center">
  <img src="../docs/screenshots/hero-banner.png" width="80%" alt="banner" />
</p>

## Quick navigation

### Architecture
- [Architecture](Architecture.md) — high-level view, request flow, diagrams.
- [Database Schema](Database-Schema.md) — entities, relations, migrations.

### Backend (Spring Boot)
- [Backend](Backend.md) — package layout, layers, schedulers, email.
- [Authentication and Security](Authentication-and-Security.md) — JWT cookies, AES-256, rate limiting.
- [API Reference](API-Reference.md) — REST and WebSocket endpoints.

### Frontend (Angular)
- [Frontend](Frontend.md) — feature modules, RxJS patterns, layout breakpoints.

### AI / ML
- [ML Service](ML-Service.md) — PyTorch MLP for category auto-suggest.
- [Chatbot](Chatbot.md) — LangChain and Chroma RAG chatbot.

### Domain
- [Promotion System](Promotion-System.md) — packages, credit, expiry job.

### DevOps
- [Deployment](Deployment.md) — VPS, Nginx, SSL, backups.
- [Configuration](Configuration.md) — environment variables per service.

### Specification
- [Use Cases](Use-Cases.md) — 72 use cases.

---

## Documentation conventions

- **Code blocks** use syntax highlighting (`java`, `typescript`, `python`, `properties`, `bash`).
- **Links** between Wiki pages are relative (`[Name](Page.md)`).
- **Screenshots** live in [`docs/screenshots/`](../docs/screenshots/README.md).
- All UI copy and email content is written in **Serbian (Latin script)**.

## Contributing to the documentation

1. Edit the relevant `.md` file under `wiki/`.
2. If you add a new page, list it in [Home.md](Home.md) and in the main [README.md](../README.md) table.
3. Commit and push.
4. (Optional) Synchronise with the GitHub Wiki repository (see the README section "Documentation (Wiki)").
