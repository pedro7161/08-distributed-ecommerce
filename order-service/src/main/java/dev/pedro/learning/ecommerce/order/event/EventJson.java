package dev.pedro.learning.ecommerce.order.event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
@ApplicationScoped
public class EventJson {
    private final ObjectMapper objectMapper;
    public EventJson(ObjectMapper objectMapper) { this.objectMapper = objectMapper; }
    public RawEventEnvelope parseRaw(String json) {
        try { return objectMapper.readValue(json, RawEventEnvelope.class); }
        catch (JsonProcessingException e) { throw new IllegalArgumentException("Invalid event JSON", e); }
    }
    public <T> EventEnvelope<T> toTyped(RawEventEnvelope raw, Class<T> payloadType) {
        try {
            T payload = objectMapper.treeToValue(raw.payload(), payloadType);
            return new EventEnvelope<>(raw.eventId(), raw.correlationId(), raw.eventType(), raw.occurredAt(), raw.schemaVersion(), payload);
        } catch (JsonProcessingException e) { throw new IllegalArgumentException("Invalid event payload", e); }
    }
}
