package dev.pedro.learning.ecommerce.order.event;
import java.util.UUID;
public record StockReservationFailedPayload(UUID orderId, UUID productId, int quantity, String reason) {}
