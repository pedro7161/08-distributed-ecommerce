package dev.pedro.learning.ecommerce.order.inbox;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="inbox_event")
public class InboxEvent extends PanacheEntityBase {
    @Id @Column(name="event_id") public UUID eventId;
    @Column(name="event_type", nullable=false) public String eventType;
    @Column(name="received_at", nullable=false) public Instant receivedAt;
    @Column(name="processed_at") public Instant processedAt;
}
