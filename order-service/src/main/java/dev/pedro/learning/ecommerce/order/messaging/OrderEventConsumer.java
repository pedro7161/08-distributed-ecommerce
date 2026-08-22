package dev.pedro.learning.ecommerce.order.messaging;
import dev.pedro.learning.ecommerce.order.application.OrderSagaService;
import dev.pedro.learning.ecommerce.order.event.*;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
@ApplicationScoped
public class OrderEventConsumer {
    private final EventJson json; private final OrderSagaService saga;
    public OrderEventConsumer(EventJson json, OrderSagaService saga){this.json=json;this.saga=saga;}
    @Incoming("inventory-events-order") @Blocking public void inventoryEvent(String value){
        RawEventEnvelope raw=json.parseRaw(value);
        switch(raw.eventType()){
            case "StockReservationFailed" -> saga.stockReservationFailed(json.toTyped(raw,StockReservationFailedPayload.class));
            case "StockReleased" -> saga.stockReleased(json.toTyped(raw,StockReleasedPayload.class));
            default -> { }
        }
    }
    @Incoming("payment-events-order") @Blocking public void paymentEvent(String value){
        RawEventEnvelope raw=json.parseRaw(value);
        if("PaymentCompleted".equals(raw.eventType())) saga.paymentCompleted(json.toTyped(raw,PaymentCompletedPayload.class));
    }
}
