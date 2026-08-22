package dev.pedro.learning.ecommerce.inventory.messaging;
import dev.pedro.learning.ecommerce.inventory.application.InventorySagaService; import dev.pedro.learning.ecommerce.inventory.event.*; import io.smallrye.common.annotation.Blocking; import jakarta.enterprise.context.ApplicationScoped; import org.eclipse.microprofile.reactive.messaging.Incoming;
@ApplicationScoped public class InventoryEventConsumer { private final EventJson json; private final InventorySagaService saga; public InventoryEventConsumer(EventJson json,InventorySagaService saga){this.json=json;this.saga=saga;}
 @Incoming("order-events-inventory") @Blocking public void orderEvent(String value){RawEventEnvelope raw=json.parseRaw(value);if("OrderCreated".equals(raw.eventType()))saga.onOrderCreated(json.toTyped(raw,OrderCreatedPayload.class));}
 @Incoming("payment-events-inventory") @Blocking public void paymentEvent(String value){RawEventEnvelope raw=json.parseRaw(value);if("PaymentFailed".equals(raw.eventType()))saga.onPaymentFailed(json.toTyped(raw,PaymentFailedPayload.class));}
}
