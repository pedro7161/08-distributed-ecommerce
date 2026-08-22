package dev.pedro.learning.ecommerce.inventory.event;
import java.util.UUID; public record StockReservationFailedPayload(UUID orderId, UUID productId, int quantity, String reason) {}
