package dev.pedro.learning.ecommerce.order.event;
import java.util.UUID;
public record PaymentCompletedPayload(UUID orderId, UUID paymentId, String providerReference) {}
