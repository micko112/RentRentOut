# Deployment Pipeline

Automatski CI/CD za Rent Rent Out. Pipeline definisan u [.github/workflows/ci.yml](.github/workflows/ci.yml).

## Šta radi

```
push u main
    │
    ├── test-backend        (mvn test)          ─┐
    │                                            │ paralelno
    ├── test-frontend       (npm run build)     ─┘
    │
    ├── build-and-push      (3 image-a → GHCR)   matrix
    │       ├── rentrentout-backend
    │       ├── rentrentout-frontend
    │       └── rentrentout-ml
    │
    └── deploy              (SSH → /opt/app/deploy.sh → smoke test)
```

- PR-ovi pokreću **samo testove** (build+push+deploy se preskaču).
- Manuelni re-deploy: **Actions → CI / CD → Run workflow** ili re-run pojedinačnog joba.
- Rollback: re-run starijeg workflow run-a (image je taggovan po SHA).

## Jednokratni setup (uradi jednom)

### 1. Generiši deploy SSH ključ (lokalno)

```bash
ssh-keygen -t ed25519 -f ~/.ssh/rentrentout_deploy -N "" -C "github-actions-deploy"
```

Dobiješ `~/.ssh/rentrentout_deploy` (private) i `~/.ssh/rentrentout_deploy.pub` (public).

### 2. Public ključ na VPS

```bash
ssh root@178.104.97.101
mkdir -p ~/.ssh && chmod 700 ~/.ssh
echo "<sadržaj rentrentout_deploy.pub>" >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
```

### 3. GHCR Personal Access Token

Trebaš PAT sa `read:packages` scope da VPS može da pull-uje private image-e:

1. GitHub → Settings → Developer settings → **Personal access tokens (classic)** → Generate new token
2. Scope: `read:packages`
3. Copy token
4. Na VPS-u dodaj u `/opt/app/RentRentOut/.env`:
   ```
   GHCR_USER=micko112
   GHCR_TOKEN=ghp_...
   ```

### 4. Kopiraj deploy.sh na VPS

```bash
scp deploy.sh root@178.104.97.101:/opt/app/deploy.sh
ssh root@178.104.97.101 "chmod +x /opt/app/deploy.sh"
```

(Ili posle prvog `git pull`-a: `chmod +x /opt/app/deploy.sh` — fajl je u repo-u.)

### 5. GitHub Secrets

Repo → **Settings → Secrets and variables → Actions → New repository secret**:

| Secret | Vrednost |
|---|---|
| `SSH_HOST` | `178.104.97.101` |
| `SSH_USER` | `root` (ili `deploy` ako napraviš dedicated user) |
| `SSH_PRIVATE_KEY` | Kompletan sadržaj `~/.ssh/rentrentout_deploy` (uključi `-----BEGIN...-----` i `-----END...-----`) |
| `SSH_PORT` | `22` (opciono — default je 22) |

### 6. GitHub Environment (opciono ali preporučeno)

Repo → **Settings → Environments → New environment → `production`**.

Dodaj **Required reviewers** (samo ti) ako želiš manual approval pre svakog deploya na prod.

### 7. Prvo pokretanje

Push bilo šta u main (ili trigger workflow ručno). Prvi run će:
- Trajati ~8–12 min (bez GHA cache-a)
- Build-ovati 3 image-a
- Push-ovati u `ghcr.io/micko112/rentrentout-*`
- SSH-ovati u VPS i pokrenuti deploy.sh
- Verifikovati sa smoke test-om

Sledeći runovi su ~3–5 min zahvaljujući GHA layer cache-u.

## Local dev (bez promene)

`docker-compose.yml` (ne prod!) i dalje builduje lokalno:

```bash
docker-compose up --build
```

## Troubleshooting

**"unauthorized" pri pull-u na VPS-u**
- PAT je istekao ili nema `read:packages`
- Ponovi step 3 i update-uj `.env`, pa `docker login ghcr.io` ručno

**"host key verification failed" u GitHub Actions**
- `appleboy/ssh-action` automatski prihvata host key, ali ako pukne: proveri `SSH_HOST` (samo IP, bez `https://`)

**Deploy prošao ali smoke test failed**
- Backend duže starta od 100s → dodaj retry u smoke test-u ili proveri `docker logs rentrentout-backend`

**Rollback**
- GitHub → Actions → nađi zeleni run pre bug-a → **Re-run all jobs**
- Ili SSH: `IMAGE_TAG=<stariji-sha> ./deploy.sh <stariji-sha>`

## Šta pipeline NE radi (svesno)

- **Ne dira `.env`** — sekreti ostaju na VPS-u, van gita
- **Ne pokreće migracije eksplicitno** — Liquibase to radi na startu backend containera
- **Ne backup-uje DB pre deploya** — `backup.sh` cron radi 02:00 svaki dan
- **Ne šalje notifikacije** — dodaj Slack/Discord webhook u deploy job ako želiš
