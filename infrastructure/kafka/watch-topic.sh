#!/usr/bin/env bash
set -euo pipefail
TOPIC="${1:?usage: $0 <topic>}"
docker compose exec -T kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server kafka:29092 --topic "$TOPIC" --from-beginning --property print.key=true --property key.separator=' | '
