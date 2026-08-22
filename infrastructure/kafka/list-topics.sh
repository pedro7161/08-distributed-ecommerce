#!/usr/bin/env bash
set -euo pipefail
docker compose exec -T kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka:29092 --list
