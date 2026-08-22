package dev.pedro.learning.ecommerce.inventory.event;
import com.fasterxml.jackson.databind.JsonNode; import java.time.Instant; import java.util.UUID;
public record RawEventEnvelope(UUID eventId, UUID correlationId, String eventType, Instant occurredAt, int schemaVersion, JsonNode payload) {}
