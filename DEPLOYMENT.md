# Deployment pipeline

Automated CI/CD for Rent Rent Out. The pipeline is defined in [.github/workflows/ci.yml](.github/workflows/ci.yml).

## What it does

```
push to main
    |
    +-- test-backend        (mvn test)          -+
    |                                            | parallel
    +-- test-frontend       (npm run build)     -+
    |
    +-- build-and-push      (3 images -> GHCR)   matrix
    |       +-- rentrentout-backend
    |       +-- rentrentout-frontend
    |       +-- rentrentout-ml
    |
    +-- deploy              (SSH -> /opt/app/deploy.sh -> smoke test)
```

- Pull requests trigger **tests only**; build, push and deploy are skipped.
- Manual re-deploy: **Actions -> CI / CD -> Run workflow**, or re-run an individual job.
- Rollback: re-run an older workflow run (images are tagged by commit SHA).

## One-time setup

### 1. Generate a deploy SSH key (locally)

```bash
ssh-keygen -t ed25519 -f ~/.ssh/rentrentout_deploy -N "" -C "github-actions-deploy"
```

This produces `~/.ssh/rentrentout_deploy` (private) and `~/.ssh/rentrentout_deploy.pub` (public).

### 2. Install the public key on the VPS

```bash
ssh root@178.104.97.101
mkdir -p ~/.ssh && chmod 700 ~/.ssh
echo "<contents of rentrentout_deploy.pub>" >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
```

### 3. GHCR Personal Access Token

The VPS needs a PAT with the `read:packages` scope in order to pull private images:

1. GitHub -> Settings -> Developer settings -> **Personal access tokens (classic)** -> Generate new token.
2. Scope: `read:packages`.
3. Copy the token.
4. On the VPS add it to `/opt/app/RentRentOut/.env`:
   ```
   GHCR_USER=micko112
   GHCR_TOKEN=ghp_...
   ```

### 4. Copy deploy.sh to the VPS

```bash
scp deploy.sh root@178.104.97.101:/opt/app/deploy.sh
ssh root@178.104.97.101 "chmod +x /opt/app/deploy.sh"
```

(Alternatively, after the first `git pull`: `chmod +x /opt/app/deploy.sh` — the file is versioned in the repository.)

### 5. GitHub Secrets

Repo -> **Settings -> Secrets and variables -> Actions -> New repository secret**:

| Secret | Value |
|---|---|
| `SSH_HOST` | `178.104.97.101` |
| `SSH_USER` | `root` (or `deploy` if you create a dedicated user) |
| `SSH_PRIVATE_KEY` | Full contents of `~/.ssh/rentrentout_deploy` (including `-----BEGIN...-----` and `-----END...-----`) |
| `SSH_PORT` | `22` (optional; default is 22) |

### 6. GitHub Environment (optional but recommended)

Repo -> **Settings -> Environments -> New environment -> `production`**.

Add **Required reviewers** (yourself) if you want manual approval before every production deploy.

### 7. First run

Push anything to `main` (or trigger the workflow manually). The first run will:
- Take roughly 8-12 minutes (no GHA cache yet).
- Build all three images.
- Push them to `ghcr.io/micko112/rentrentout-*`.
- SSH into the VPS and run `deploy.sh`.
- Verify with a smoke test.

Subsequent runs take about 3-5 minutes thanks to the GHA layer cache.

## Local development (unchanged)

`docker-compose.yml` (not the production file) still builds locally:

```bash
docker-compose up --build
```

## Troubleshooting

**"unauthorized" while pulling on the VPS**
- The PAT expired or lacks `read:packages`.
- Repeat step 3, update `.env`, then run `docker login ghcr.io` manually.

**"host key verification failed" in GitHub Actions**
- `appleboy/ssh-action` accepts the host key automatically; if it still fails, verify `SSH_HOST` (IP only, without `https://`).

**Deploy succeeded but the smoke test failed**
- The backend takes longer than 100 s to start; add a retry to the smoke test or inspect `docker logs rentrentout-backend`.

**Rollback**
- GitHub -> Actions -> pick a green run from before the bug -> **Re-run all jobs**.
- Or via SSH: `IMAGE_TAG=<older-sha> ./deploy.sh <older-sha>`.

## What the pipeline deliberately does not do

- **Never touches `.env`** — secrets stay on the VPS, outside of git.
- **Does not run migrations explicitly** — Liquibase does that on backend container start-up.
- **Does not back up the database before deploy** — the `backup.sh` cron runs at 02:00 every day.
- **Does not send notifications** — add a Slack or Discord webhook to the deploy job if desired.
