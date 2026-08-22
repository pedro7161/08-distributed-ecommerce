package dev.pedro.learning.ecommerce.inventory.inbox;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase; import jakarta.enterprise.context.ApplicationScoped; import java.util.UUID;
@ApplicationScoped public class InboxEventRepository implements PanacheRepositoryBase<InboxEvent,UUID> {}
