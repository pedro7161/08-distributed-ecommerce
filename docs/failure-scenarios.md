# Failure Scenarios Lab

This file is a command-oriented lab for reproducing the nine required scenarios.

## Before each lab

Infrastructure:

```bash
docker compose up -d
```

Services, each in its own terminal:

```bash
mvn -pl order-service quarkus:dev
mvn -pl inventory-service quarkus:dev
mvn -pl payment-service quarkus:dev
mvn -pl notification-service quarkus:dev
```

After Flyway has created the outbox tables:

```bash
./infrastructure/debezium/register-connectors.sh
./infrastructure/debezium/status.sh
```

Helper reset:

```bash
curl -s -X PUT http://localhost:8081/admin/inventory/11111111-1111-1111-1111-111111111111/quantity/100 | jq
curl -s -X PUT http://localhost:8082/admin/payment-provider/reset | jq
curl -s -X PUT http://localhost:8083/admin/email-provider/reset | jq
```

Helper order command:

```bash
CORRELATION_ID=$(uuidgen)
ORDER_RESPONSE=$(curl -s -X POST http://localhost:8080/orders \
  -H 'Content-Type: application/json' \
  -H "X-Correlation-ID: $CORRELATION_ID" \
  -d '{"customerEmail":"learner@example.com","productId":"11111111-1111-1111-1111-111111111111","quantity":2,"unitPrice":25.00}')
ORDER_ID=$(echo "$ORDER_RESPONSE" | jq -r .id)
echo "$ORDER_RESPONSE" | jq
```

## 1. Successful order

```bash
curl -s -X PUT http://localhost:8082/admin/payment-provider/reset | jq
curl -s -X PUT http://localhost:8083/admin/email-provider/reset | jq
curl -s -X PUT http://localhost:8081/admin/inventory/11111111-1111-1111-1111-111111111111/quantity/10 | jq
```

Run the helper order command.

Immediately:

```bash
curl -s http://localhost:8080/orders/$ORDER_ID | jq
```

Expected: `PENDING`.

A few seconds later:

```bash
sleep 3
curl -s http://localhost:8080/orders/$ORDER_ID | jq
curl -s http://localhost:8082/payments/order/$ORDER_ID | jq
curl -s http://localhost:8083/notifications/order/$ORDER_ID | jq
```

Expected: `CONFIRMED`, `COMPLETED`, one successful notification.

## 2. Insufficient inventory

```bash
curl -s -X PUT http://localhost:8081/admin/inventory/11111111-1111-1111-1111-111111111111/quantity/0 | jq
```

Run the helper order command, then:

```bash
sleep 2
curl -s http://localhost:8080/orders/$ORDER_ID | jq
```

Expected flow:

```text
OrderCreated
StockReservationFailed
OrderCancelled
```

No payment row should exist for the order.

## 3. Payment failure without provider outage

Use a normal business decline:

```bash
curl -s -X PUT http://localhost:8082/admin/payment-provider/reset | jq
curl -s -X PUT http://localhost:8082/admin/payment-provider/mode/DECLINE | jq
curl -s -X PUT http://localhost:8081/admin/inventory/11111111-1111-1111-1111-111111111111/quantity/10 | jq
```

Run the helper order command.

```bash
sleep 3
curl -s http://localhost:8082/payments/order/$ORDER_ID | jq
curl -s http://localhost:8081/reservations/$ORDER_ID | jq
curl -s http://localhost:8080/orders/$ORDER_ID | jq
```

Expected:

```text
payment.status     = FAILED
reservation.status = RELEASED
order.status       = CANCELLED
```

The compensation sequence is:

```text
PaymentFailed -> StockReleased -> OrderCancelled
```

## 4. Provider unavailable and circuit breaker

```bash
curl -s -X PUT http://localhost:8082/admin/payment-provider/reset | jq
curl -s -X PUT http://localhost:8082/admin/payment-provider/mode/ALWAYS_FAIL | jq
curl -s -X PUT http://localhost:8081/admin/inventory/11111111-1111-1111-1111-111111111111/quantity/100 | jq
```

Create eight orders quickly:

```bash
for i in $(seq 1 8); do
  curl -s -X POST http://localhost:8080/orders \
    -H 'Content-Type: application/json' \
    -d '{"customerEmail":"learner@example.com","productId":"11111111-1111-1111-1111-111111111111","quantity":1,"unitPrice":10.00}' >/dev/null
done
```

Inspect the provider/gateway counters:

```bash
curl -s http://localhost:8082/admin/payment-provider/stats | jq
```

While the circuit is open, additional gateway calls are handled by fallback without contacting `/fake-payment-provider/payments`. Therefore `gatewayFallbacks` increases faster than `providerCalls`.

Wait longer than the configured three-second circuit delay and restore the provider:

```bash
sleep 4
curl -s -X PUT http://localhost:8082/admin/payment-provider/mode/NORMAL | jq
```

Create a few more orders. Successful probe requests eventually close the breaker.

## 5. Duplicate Kafka event

First create at least one order.

Capture a value from `order-events`:

```bash
EVENT=$(docker compose exec -T kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server kafka:29092 --topic order-events --from-beginning \
  --max-messages 1 --timeout-ms 5000 2>/dev/null)
```

Replay the exact JSON event:

```bash
printf '%s\n' "$EVENT" | docker compose exec -T kafka \
  /opt/kafka/bin/kafka-console-producer.sh \
  --bootstrap-server kafka:29092 --topic order-events
```

Expected Inventory log:

```text
[INVENTORY] Duplicate OrderCreated ignored
```

Because the duplicate has the same `eventId`, `inbox_event` blocks a second stock mutation.

## 6. Kafka temporarily unavailable

Stop Kafka only:

```bash
docker compose stop kafka
```

Create an order with the helper command.

The request still returns `201` because the Order Service transaction only needs Order PostgreSQL.

Verify:

```bash
curl -s http://localhost:8080/orders/$ORDER_ID | jq
```

Expected while Kafka is down: `PENDING`.

Bring Kafka back:

```bash
docker compose start kafka
```

Watch connector recovery:

```bash
./infrastructure/debezium/status.sh
```

Then:

```bash
sleep 5
curl -s http://localhost:8080/orders/$ORDER_ID | jq
```

Expected: the outbox is eventually published and the normal Saga resumes.

## 7. Notification transient failure

```bash
curl -s -X PUT http://localhost:8083/admin/email-provider/reset | jq
curl -s -X PUT http://localhost:8083/admin/email-provider/fail-first/2 | jq
curl -s -X PUT http://localhost:8082/admin/payment-provider/reset | jq
```

Run the helper order command.

```bash
sleep 4
curl -s http://localhost:8083/notifications/order/$ORDER_ID | jq
curl -s http://localhost:8083/admin/email-provider/stats | jq
```

Expected: notification exists and its `attempts` field is `3`.

## 8. Notification permanent failure and DLQ

```bash
curl -s -X PUT http://localhost:8083/admin/email-provider/reset | jq
curl -s -X PUT http://localhost:8083/admin/email-provider/mode/ALWAYS_FAIL | jq
curl -s -X PUT http://localhost:8082/admin/payment-provider/reset | jq
```

Run the helper order command.

After retry exhaustion:

```bash
sleep 5
docker compose exec -T kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server kafka:29092 --topic notification-dlq \
  --from-beginning --max-messages 1
```

Expected structure:

```json
{
  "eventType": "NotificationDeliveryFailed",
  "correlationId": "...",
  "payload": {
    "originalEvent": {},
    "error": "...",
    "attempts": 4,
    "failedAt": "..."
  }
}
```

## 9. Service restart during processing

Configure the email provider to hold a message for ten seconds:

```bash
curl -s -X PUT http://localhost:8083/admin/email-provider/reset | jq
curl -s -X PUT http://localhost:8083/admin/email-provider/delay/10000 | jq
curl -s -X PUT http://localhost:8083/admin/email-provider/mode/SLOW | jq
```

Run the helper order command.

When Notification Service begins processing `OrderConfirmed`, stop its dev-mode process before the provider call returns.

Restart it:

```bash
mvn -pl notification-service quarkus:dev
```

The Kafka offset was not successfully completed and the local notification transaction did not commit. The record is therefore redelivered after restart and can be processed again.

This scenario is a useful way to see why inbox state must be committed with the business result rather than before it.

## Cleanup

Return provider modes to normal:

```bash
curl -s -X PUT http://localhost:8082/admin/payment-provider/reset | jq
curl -s -X PUT http://localhost:8083/admin/email-provider/reset | jq
```

Destroy all persisted lab state when you want a clean slate:

```bash
docker compose down -v
```
