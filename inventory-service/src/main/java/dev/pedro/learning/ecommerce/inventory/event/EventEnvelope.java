package dev.pedro.learning.ecommerce.inventory.event;
import java.time.Instant; import java.util.UUID;
public record EventEnvelope<T>(UUID eventId, UUID correlationId, String eventType, Instant occurredAt, int schemaVersion, T payload) {}
