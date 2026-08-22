#!/usr/bin/env bash
set -euo pipefail
BASE_URL="${CONNECT_URL:-http://localhost:8084}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

until curl -fsS "$BASE_URL/connectors" >/dev/null; do
  sleep 1
done

for service in order inventory payment; do
  echo "Registering ${service}-outbox connector"
  curl -fsS -X PUT \
    -H 'Content-Type: application/json' \
    --data "@$SCRIPT_DIR/${service}-outbox.json" \
    "$BASE_URL/connectors/${service}-outbox/config" | python -m json.tool
done
