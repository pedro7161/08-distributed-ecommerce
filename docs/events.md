# Event Catalogue

## Envelope

Every domain or technical event has:

| Field | Type | Meaning |
|---|---|---|
| `eventId` | UUID | Unique identifier used by inbox/idempotency logic |
| `correlationId` | UUID | Stable identifier for the whole distributed flow |
| `eventType` | string | Event discriminator |
| `occurredAt` | ISO-8601 instant | Event creation time |
| `schemaVersion` | integer | Payload schema version |
| `payload` | typed object | Event-specific data |

Example:

```json
{
  "eventId": "54b0f19f-0d23-48e0-bbb3-eb4805bb3033",
  "correlationId": "4ac92a70-5434-40dc-aea0-da88f31f7746",
  "eventType": "OrderCreated",
  "occurredAt": "2026-08-22T18:00:00Z",
  "schemaVersion": 1,
  "payload": {
    "orderId": "be06ba46-a031-43f9-ac54-365451a9fa6e",
    "productId": "11111111-1111-1111-1111-111111111111",
    "quantity": 2,
    "unitPrice": 25.00,
    "totalAmount": 50.00,
    "customerEmail": "learner@example.com"
  }
}
```

## Order events

### `OrderCreated`

Producer: Order Service  
Topic: `order-events`

Payload:

```text
orderId
productId
quantity
unitPrice
totalAmount
customerEmail
```

### `OrderConfirmed`

Producer: Order Service  
Topic: `order-events`

Payload:

```text
orderId
customerEmail
totalAmount
```

### `OrderCancelled`

Producer: Order Service  
Topic: `order-events`

Payload:

```text
orderId
reason
```

`OrderCancelled` is useful for observation and future downstream consumers; no current service needs it to complete the core flow.

## Inventory events

### `StockReserved`

Producer: Inventory Service  
Topic: `inventory-events`

Payload:

```text
orderId
productId
quantity
totalAmount
customerEmail
```

The total and email are copied from `OrderCreated` so Payment does not need to query Order Service or Order DB.

### `StockReservationFailed`

Producer: Inventory Service  
Topic: `inventory-events`

Payload:

```text
orderId
productId
quantity
reason
```

### `StockReleased`

Producer: Inventory Service  
Topic: `inventory-events`

Payload:

```text
orderId
productId
quantity
```

This is the observable completion event for the compensating transaction.

## Payment events

### `PaymentCompleted`

Producer: Payment Service  
Topic: `payment-events`

Payload:

```text
orderId
paymentId
providerReference
```

### `PaymentFailed`

Producer: Payment Service  
Topic: `payment-events`

Payload:

```text
orderId
paymentId
reason
```

Inventory consumes this event to start the compensation.

## Notification DLQ event

### `NotificationDeliveryFailed`

Producer: Notification Service  
Topic: `notification-dlq`

Payload:

```text
originalEvent
error
attempts
failedAt
```

`originalEvent` is the complete raw `OrderConfirmed` envelope, preserving its original event ID, correlation ID and payload for debugging or manual replay.

## Ordering

The outbox uses `aggregate_id` as the Kafka message key. For these flows it is the order ID. Kafka therefore keeps events for one order on the same partition of each topic, preserving per-order ordering within that topic.

Cross-topic ordering is not assumed. State changes are driven only by the explicit event dependencies described in the Saga.
