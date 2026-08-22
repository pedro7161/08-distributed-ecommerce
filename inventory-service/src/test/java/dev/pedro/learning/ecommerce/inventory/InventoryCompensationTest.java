package dev.pedro.learning.ecommerce.inventory;

import dev.pedro.learning.ecommerce.inventory.application.InventorySagaService;
import dev.pedro.learning.ecommerce.inventory.domain.*;
import dev.pedro.learning.ecommerce.inventory.event.*;
import dev.pedro.learning.ecommerce.inventory.inbox.InboxEventRepository;
import dev.pedro.learning.ecommerce.inventory.outbox.OutboxEventRepository;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class InventoryCompensationTest {
    static final UUID PRODUCT = UUID.fromString("11111111-1111-1111-1111-111111111111");
    @Inject InventorySagaService saga;
    @Inject InventoryItemRepository items;
    @Inject InventoryReservationRepository reservations;
    @Inject InboxEventRepository inbox;
    @Inject OutboxEventRepository outbox;

    @BeforeEach
    void reset() {
        QuarkusTransaction.requiringNew().run(() -> {
            outbox.deleteAll();
            inbox.deleteAll();
            reservations.deleteAll();
            items.deleteAll();
            InventoryItem item = new InventoryItem();
            item.productId = PRODUCT;
            item.availableQuantity = 10;
            item.reservedQuantity = 0;
            items.persist(item);
        });
    }

    @Test
    void paymentFailureReleasesExactlyTheReservedStock() {
        UUID orderId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        saga.onOrderCreated(new EventEnvelope<>(UUID.randomUUID(), correlationId, "OrderCreated", Instant.now(), 1,
                new OrderCreatedPayload(orderId, PRODUCT, 3, new BigDecimal("5.00"), new BigDecimal("15.00"), "learner@example.com")));

        EventEnvelope<PaymentFailedPayload> paymentFailed = new EventEnvelope<>(UUID.randomUUID(), correlationId, "PaymentFailed", Instant.now(), 1,
                new PaymentFailedPayload(orderId, UUID.randomUUID(), "declined"));
        saga.onPaymentFailed(paymentFailed);
        saga.onPaymentFailed(paymentFailed);

        InventoryItem item = items.findById(PRODUCT);
        assertEquals(10, item.availableQuantity);
        assertEquals(0, item.reservedQuantity);
        assertEquals(1, outbox.count("type", "StockReleased"));
    }
}
