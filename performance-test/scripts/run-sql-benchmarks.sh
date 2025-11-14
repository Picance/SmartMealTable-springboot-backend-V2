#!/usr/bin/env bash
set -euo pipefail

if ! command -v envsubst >/dev/null 2>&1; then
  echo "envsubst is required (gettext)." >&2
  exit 1
fi

if ! command -v mysql >/dev/null 2>&1; then
  echo "mysql CLI is required. Install via brew install mysql-client." >&2
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TEMPLATE="$SCRIPT_DIR/../sql/core_workloads.sql.tpl"

: "${DB_HOST:=127.0.0.1}"
: "${DB_PORT:=3306}"
: "${DB_USER:=root}"
: "${DB_PASSWORD:=root123}"
: "${DB_NAME:=smartmealtable}"
: "${MEMBER_ID:=1}"
: "${START_DATE:=2025-01-01}"
: "${END_DATE:=2025-03-31}"
: "${FOOD_PREFIX:=PERF}" 
: "${BUDGET_MONTH:=2025-02}"
: "${DAILY_DATE:=2025-02-15}"

TEMP_SQL="$(mktemp)"
trap 'rm -f "$TEMP_SQL"' EXIT

envsubst < "$TEMPLATE" > "$TEMP_SQL"

echo "Running EXPLAIN ANALYZE workloads against ${DB_NAME} (${DB_HOST}:${DB_PORT})"
mysql --host="$DB_HOST" --port="$DB_PORT" --user="$DB_USER" --password="$DB_PASSWORD" "$DB_NAME" < "$TEMP_SQL"
