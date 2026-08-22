package dev.pedro.learning.ecommerce.inventory.event;
import java.util.UUID; public record StockReleasedPayload(UUID orderId, UUID productId, int quantity) {}
