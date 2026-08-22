package dev.pedro.learning.ecommerce.order.outbox;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.pedro.learning.ecommerce.order.event.EventEnvelope;
import jakarta.enterprise.context.ApplicationScoped;
@ApplicationScoped
public class OutboxWriter {
    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;
    public OutboxWriter(OutboxEventRepository repository, ObjectMapper objectMapper) { this.repository=repository; this.objectMapper=objectMapper; }
    public void append(String topic, String aggregateId, EventEnvelope<?> envelope) {
        OutboxEvent event = new OutboxEvent();
        event.id=envelope.eventId(); event.aggregateType=topic; event.aggregateId=aggregateId; event.type=envelope.eventType();
        event.payload=objectMapper.valueToTree(envelope); event.occurredAt=envelope.occurredAt(); repository.persist(event);
    }
}
