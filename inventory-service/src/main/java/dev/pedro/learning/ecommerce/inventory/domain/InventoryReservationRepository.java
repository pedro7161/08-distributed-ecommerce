package dev.pedro.learning.ecommerce.inventory.domain;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase; import jakarta.enterprise.context.ApplicationScoped; import java.util.UUID;
@ApplicationScoped public class InventoryReservationRepository implements PanacheRepositoryBase<InventoryReservation,UUID> { public InventoryReservation findByOrderId(UUID orderId){return find("orderId",orderId).firstResult();} }
