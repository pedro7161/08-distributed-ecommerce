#!/usr/bin/env bash
set -euo pipefail
BASE_URL="${CONNECT_URL:-http://localhost:8084}"
for service in order inventory payment; do
  echo "=== ${service}-outbox ==="
  curl -fsS "$BASE_URL/connectors/${service}-outbox/status" | python -m json.tool
done
