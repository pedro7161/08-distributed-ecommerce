package dev.pedro.learning.ecommerce.order.event;
import java.math.BigDecimal;
import java.util.UUID;
public record OrderConfirmedPayload(UUID orderId, String customerEmail, BigDecimal totalAmount) {}
