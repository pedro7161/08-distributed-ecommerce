package dev.pedro.learning.ecommerce.order.application;

import dev.pedro.learning.ecommerce.order.domain.OrderEntity;
import dev.pedro.learning.ecommerce.order.domain.OrderRepository;
import dev.pedro.learning.ecommerce.order.domain.OrderStatus;
import dev.pedro.learning.ecommerce.order.event.EventEnvelope;
import dev.pedro.learning.ecommerce.order.event.OrderCancelledPayload;
import dev.pedro.learning.ecommerce.order.event.OrderConfirmedPayload;
import dev.pedro.learning.ecommerce.order.event.PaymentCompletedPayload;
import dev.pedro.learning.ecommerce.order.event.StockReleasedPayload;
import dev.pedro.learning.ecommerce.order.event.StockReservationFailedPayload;
import dev.pedro.learning.ecommerce.order.inbox.InboxEvent;
import dev.pedro.learning.ecommerce.order.inbox.InboxEventRepository;
import dev.pedro.learning.ecommerce.order.outbox.OutboxWriter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class OrderSagaService {
    private static final Logger LOG = Logger.getLogger(OrderSagaService.class);

    private final OrderRepository orders;
    private final InboxEventRepository inbox;
    private final OutboxWriter outbox;

    public OrderSagaService(OrderRepository orders, InboxEventRepository inbox, OutboxWriter outbox) {
        this.orders = orders;
        this.inbox = inbox;
        this.outbox = outbox;
    }

    @Transactional
    public void stockReservationFailed(EventEnvelope<StockReservationFailedPayload> event) {
        withCorrelation(event, () -> {
            if (begin(event)) {
                return;
            }
            OrderEntity order = requireOrder(event.payload().orderId());
            order.status = OrderStatus.CANCELLED;
            order.cancellationReason = event.payload().reason();
            order.updatedAt = Instant.now();

            var cancelled = new EventEnvelope<>(
                    UUID.randomUUID(),
                    event.correlationId(),
                    "OrderCancelled",
                    Instant.now(),
                    1,
                    new OrderCancelledPayload(order.id, event.payload().reason()));
            outbox.append("order-events", order.id.toString(), cancelled);
            finish(event);
            LOG.info("[ORDER] OrderCancelled: insufficient inventory");
        });
    }

    @Transactional
    public void paymentCompleted(EventEnvelope<PaymentCompletedPayload> event) {
        withCorrelation(event, () -> {
            if (begin(event)) {
                return;
            }
            OrderEntity order = requireOrder(event.payload().orderId());
            order.status = OrderStatus.CONFIRMED;
            order.updatedAt = Instant.now();

            var confirmed = new EventEnvelope<>(
                    UUID.randomUUID(),
                    event.correlationId(),
                    "OrderConfirmed",
                    Instant.now(),
                    1,
                    new OrderConfirmedPayload(order.id, order.customerEmail, order.totalAmount));
            outbox.append("order-events", order.id.toString(), confirmed);
            finish(event);
            LOG.info("[ORDER] OrderConfirmed");
        });
    }

    @Transactional
    public void stockReleased(EventEnvelope<StockReleasedPayload> event) {
        withCorrelation(event, () -> {
            if (begin(event)) {
                return;
            }
            OrderEntity order = requireOrder(event.payload().orderId());
            order.status = OrderStatus.CANCELLED;
            order.cancellationReason = "Payment failed; inventory compensation completed";
            order.updatedAt = Instant.now();

            var cancelled = new EventEnvelope<>(
                    UUID.randomUUID(),
                    event.correlationId(),
                    "OrderCancelled",
                    Instant.now(),
                    1,
                    new OrderCancelledPayload(order.id, order.cancellationReason));
            outbox.append("order-events", order.id.toString(), cancelled);
            finish(event);
            LOG.info("[ORDER] OrderCancelled");
        });
    }

    private boolean begin(EventEnvelope<?> event) {
        if (inbox.findById(event.eventId()) != null) {
            LOG.infof("[ORDER] Duplicate %s ignored", event.eventType());
            return true;
        }

        InboxEvent row = new InboxEvent();
        row.eventId = event.eventId();
        row.eventType = event.eventType();
        row.receivedAt = Instant.now();
        inbox.persist(row);
        return false;
    }

    private void finish(EventEnvelope<?> event) {
        InboxEvent row = inbox.findById(event.eventId());
        if (row != null) {
            row.processedAt = Instant.now();
        }
    }

    private OrderEntity requireOrder(UUID orderId) {
        OrderEntity order = orders.findById(orderId);
        if (order == null) {
            throw new IllegalStateException("Order not found for Saga event: " + orderId);
        }
        return order;
    }

    private void withCorrelation(EventEnvelope<?> event, Runnable action) {
        MDC.put("correlationId", event.correlationId().toString());
        try {
            action.run();
        } finally {
            MDC.remove("correlationId");
        }
    }
}
