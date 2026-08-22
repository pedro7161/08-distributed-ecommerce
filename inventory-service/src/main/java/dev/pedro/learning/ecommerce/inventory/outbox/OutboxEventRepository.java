package dev.pedro.learning.ecommerce.inventory.outbox;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase; import jakarta.enterprise.context.ApplicationScoped; import java.util.UUID;
@ApplicationScoped public class OutboxEventRepository implements PanacheRepositoryBase<OutboxEvent,UUID> {}
