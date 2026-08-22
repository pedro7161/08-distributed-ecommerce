package dev.pedro.learning.ecommerce.inventory.domain;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase; import jakarta.persistence.*; import java.util.UUID;
@Entity @Table(name="inventory_item") public class InventoryItem extends PanacheEntityBase { @Id @Column(name="product_id") public UUID productId; @Column(name="available_quantity",nullable=false) public int availableQuantity; @Column(name="reserved_quantity",nullable=false) public int reservedQuantity; }
