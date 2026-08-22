package dev.pedro.learning.ecommerce.order.event;
import java.util.UUID;
public record OrderCancelledPayload(UUID orderId, String reason) {}
