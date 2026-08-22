package dev.pedro.learning.ecommerce.notification.messaging;

import dev.pedro.learning.ecommerce.notification.event.EventEnvelope;
import dev.pedro.learning.ecommerce.notification.event.NotificationFailedPayload;
import dev.pedro.learning.ecommerce.notification.event.RawEventEnvelope;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class NotificationDeadLetterFactory {
    public EventEnvelope<NotificationFailedPayload> create(RawEventEnvelope originalEvent, String error, int attempts) {
        Instant failedAt = Instant.now();
        NotificationFailedPayload payload = new NotificationFailedPayload(originalEvent, error, attempts, failedAt);
        return new EventEnvelope<>(
                UUID.randomUUID(),
                originalEvent.correlationId(),
                "NotificationDeliveryFailed",
                failedAt,
                1,
                payload);
    }
}
