#!/usr/bin/env bash
# Zero-touch deploy script — pokreće ga GitHub Actions preko SSH-a.
# Očekuje se da se nalazi na VPS-u u /opt/app/deploy.sh
#
# Usage: ./deploy.sh <git-sha>
#   git-sha — koji tag da pull-uje sa GHCR-a. Ako je izostavljen, koristi :latest.

set -euo pipefail

REPO_DIR="/opt/app"
COMPOSE_FILE="docker-compose.prod.yml"
IMAGE_OWNER="${IMAGE_OWNER:-micko112}"
GIT_SHA="${1:-}"

cd "$REPO_DIR"

echo "▶ [1/6] Sync repo (za docker-compose.prod.yml, nginx.prod.conf, migracije)"
git fetch --all --prune
git reset --hard origin/main

echo "▶ [2/6] Odaberi image tag"
if [ -n "$GIT_SHA" ]; then
  export IMAGE_TAG="$GIT_SHA"
else
  export IMAGE_TAG="latest"
fi
export IMAGE_OWNER
echo "    IMAGE_OWNER=$IMAGE_OWNER"
echo "    IMAGE_TAG=$IMAGE_TAG"

echo "▶ [3/6] Login na GHCR"
# Čitamo samo GHCR_USER i GHCR_TOKEN iz .env-a — ne source-ujemo ceo fajl
# (source puca ako neki value ima $, `, <, > ili sličan bash-poseban karakter)
ENV_FILE="$REPO_DIR/RentRentOut/.env"
GHCR_USER=$(grep -E '^GHCR_USER=' "$ENV_FILE" 2>/dev/null | cut -d= -f2- | tr -d '\r' || true)
GHCR_TOKEN=$(grep -E '^GHCR_TOKEN=' "$ENV_FILE" 2>/dev/null | cut -d= -f2- | tr -d '\r' || true)
if [ -z "$GHCR_TOKEN" ] || [ -z "$GHCR_USER" ]; then
  echo "⚠ GHCR_USER ili GHCR_TOKEN nedostaju u $ENV_FILE — preskačem login (koristi cached credentials)"
else
  echo "$GHCR_TOKEN" | docker login ghcr.io -u "$GHCR_USER" --password-stdin
fi

echo "▶ [4/6] Pull najnovijih image-a"
docker compose -f "$COMPOSE_FILE" pull backend frontend ml-service

echo "▶ [5/6] Restart servisa (rolling, bez down-a MySQL-a)"
docker compose -f "$COMPOSE_FILE" up -d --no-deps --remove-orphans backend frontend ml-service

# Nginx kešira upstream DNS — restart je obavezan da bi video nove IP-jeve
# containera koji su tek zamenjeni. Bez ovoga → 502 Bad Gateway.
echo "    Restarting Nginx (upstream DNS refresh)..."
docker compose -f "$COMPOSE_FILE" restart nginx

echo "▶ [6/6] Cleanup dangling image-a"
docker image prune -f

echo "✓ Deploy završen — commit $IMAGE_TAG"
