package dev.pedro.learning.ecommerce.order.domain;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="orders")
public class OrderEntity extends PanacheEntityBase {
    @Id public UUID id;
    @Column(name="correlation_id", nullable=false) public UUID correlationId;
    @Column(name="customer_email", nullable=false) public String customerEmail;
    @Column(name="product_id", nullable=false) public UUID productId;
    @Column(nullable=false) public int quantity;
    @Column(name="unit_price", nullable=false, precision=19, scale=2) public BigDecimal unitPrice;
    @Column(name="total_amount", nullable=false, precision=19, scale=2) public BigDecimal totalAmount;
    @Enumerated(EnumType.STRING) @Column(nullable=false) public OrderStatus status;
    @Column(name="cancellation_reason") public String cancellationReason;
    @Column(name="created_at", nullable=false) public Instant createdAt;
    @Column(name="updated_at", nullable=false) public Instant updatedAt;
}
