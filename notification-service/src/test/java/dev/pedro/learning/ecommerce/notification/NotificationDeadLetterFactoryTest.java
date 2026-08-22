package dev.pedro.learning.ecommerce.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.pedro.learning.ecommerce.notification.event.RawEventEnvelope;
import dev.pedro.learning.ecommerce.notification.messaging.NotificationDeadLetterFactory;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificationDeadLetterFactoryTest {
    @Test
    void deadLetterKeepsCorrelationAndOriginalEvent() {
        UUID eventId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        RawEventEnvelope original = new RawEventEnvelope(
                eventId,
                correlationId,
                "OrderConfirmed",
                Instant.now(),
                1,
                new ObjectMapper().createObjectNode().put("orderId", UUID.randomUUID().toString()));

        var deadLetter = new NotificationDeadLetterFactory().create(original, "provider down", 4);

        assertEquals(correlationId, deadLetter.correlationId());
        assertEquals("NotificationDeliveryFailed", deadLetter.eventType());
        assertEquals(4, deadLetter.payload().attempts());
        assertEquals(eventId, deadLetter.payload().originalEvent().eventId());
        assertEquals("provider down", deadLetter.payload().error());
    }
}
