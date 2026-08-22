package dev.pedro.learning.ecommerce.inventory.event;
import java.math.BigDecimal; import java.util.UUID;
public record OrderCreatedPayload(UUID orderId, UUID productId, int quantity, BigDecimal unitPrice, BigDecimal totalAmount, String customerEmail) {}
