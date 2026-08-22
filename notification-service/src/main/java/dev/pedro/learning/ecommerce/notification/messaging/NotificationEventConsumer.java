package dev.pedro.learning.ecommerce.notification.messaging;

import dev.pedro.learning.ecommerce.notification.application.NotificationDeliveryService;
import dev.pedro.learning.ecommerce.notification.application.NotificationSagaService;
import dev.pedro.learning.ecommerce.notification.event.EventEnvelope;
import dev.pedro.learning.ecommerce.notification.event.EventJson;
import dev.pedro.learning.ecommerce.notification.event.OrderConfirmedPayload;
import dev.pedro.learning.ecommerce.notification.event.RawEventEnvelope;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

@ApplicationScoped
public class NotificationEventConsumer {
    private static final Logger LOG = Logger.getLogger(NotificationEventConsumer.class);

    private final EventJson json;
    private final NotificationSagaService saga;
    private final NotificationDeliveryService delivery;
    private final NotificationDeadLetterFactory deadLetterFactory;
    private final Emitter<String> dlq;

    public NotificationEventConsumer(
            EventJson json,
            NotificationSagaService saga,
            NotificationDeliveryService delivery,
            NotificationDeadLetterFactory deadLetterFactory,
            @Channel("notification-dlq") Emitter<String> dlq) {
        this.json = json;
        this.saga = saga;
        this.delivery = delivery;
        this.deadLetterFactory = deadLetterFactory;
        this.dlq = dlq;
    }

    @Incoming("order-events-notification")
    @Blocking
    public void orderEvent(String value) {
        RawEventEnvelope raw = json.parseRaw(value);
        if (!"OrderConfirmed".equals(raw.eventType())) {
            return;
        }

        EventEnvelope<OrderConfirmedPayload> event = json.toTyped(raw, OrderConfirmedPayload.class);
        try {
            saga.onOrderConfirmed(event);
        } catch (RuntimeException failure) {
            MDC.put("correlationId", event.correlationId().toString());
            try {
                int attempts = delivery.attempts(event.eventId());
                var deadLetter = deadLetterFactory.create(raw, failure.getMessage(), attempts);
                dlq.send(json.write(deadLetter)).toCompletableFuture().join();
                LOG.errorf("[NOTIFICATION] Permanent failure after %d attempts -> notification-dlq", attempts);
                delivery.clear(event.eventId());
            } finally {
                MDC.remove("correlationId");
            }
        }
    }
}
