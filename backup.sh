#!/bin/bash
# MySQL backup script — pokretati kao cron job na VPS-u
# Cron (dodati u crontab -e):
#   0 2 * * * /opt/app/backup.sh >> /opt/app/backups/backup.log 2>&1

set -euo pipefail

BACKUP_DIR="/opt/app/backups"
DAYS_TO_KEEP=14
DATE=$(date +"%Y-%m-%d_%H-%M-%S")
FILENAME="rentrentout_${DATE}.sql.gz"

# Učitaj env varijable
if [ -f /opt/app/.env ]; then
  export $(grep -v '^#' /opt/app/.env | xargs)
fi

DB_HOST="${MYSQL_HOST:-db}"
DB_PORT="${MYSQL_PORT:-3306}"
DB_NAME="${MYSQL_DATABASE:-rentrentout}"
DB_USER="${MYSQL_USER:-rentuser}"
DB_PASS="${MYSQL_PASSWORD}"

mkdir -p "$BACKUP_DIR"

echo "[$(date)] Backup started: $FILENAME"

docker exec "$(docker ps -qf 'name=db')" \
  mysqldump -h"$DB_HOST" -u"$DB_USER" -p"$DB_PASS" \
  --single-transaction --routines --triggers \
  "$DB_NAME" | gzip > "$BACKUP_DIR/$FILENAME"

echo "[$(date)] Backup finished: $BACKUP_DIR/$FILENAME ($(du -sh "$BACKUP_DIR/$FILENAME" | cut -f1))"

# Obrisi backup-ove starije od DAYS_TO_KEEP dana
find "$BACKUP_DIR" -name "rentrentout_*.sql.gz" -mtime "+$DAYS_TO_KEEP" -delete
echo "[$(date)] Cleanup done. Keeping last $DAYS_TO_KEEP days."

# Cron: 0 2 * * * /opt/app/backup.sh   → svaki dan u 02:00
FILENAME="rentrentout_$(date +%F_%H-%M-%S).sql.gz"

docker exec "$(docker ps -qf 'name=db')" \
  mysqldump --single-transaction --routines --triggers "$DB_NAME" \
  | gzip > "$BACKUP_DIR/$FILENAME"

# Zadrži samo poslednjih 14 dana
find "$BACKUP_DIR" -name "rentrentout_*.sql.gz" -mtime +14 -delete