package dev.pedro.learning.ecommerce.inventory.event;
import java.util.UUID; public record PaymentFailedPayload(UUID orderId, UUID paymentId, String reason) {}
