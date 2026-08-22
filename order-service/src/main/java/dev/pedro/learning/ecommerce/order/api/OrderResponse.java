package dev.pedro.learning.ecommerce.order.api;
import dev.pedro.learning.ecommerce.order.domain.OrderEntity;
import dev.pedro.learning.ecommerce.order.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
public record OrderResponse(UUID id, UUID correlationId, String customerEmail, UUID productId, int quantity, BigDecimal unitPrice, BigDecimal totalAmount, OrderStatus status, String cancellationReason, Instant createdAt, Instant updatedAt) {
    public static OrderResponse from(OrderEntity o) { return new OrderResponse(o.id,o.correlationId,o.customerEmail,o.productId,o.quantity,o.unitPrice,o.totalAmount,o.status,o.cancellationReason,o.createdAt,o.updatedAt); }
}
