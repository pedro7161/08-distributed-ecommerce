package dev.pedro.learning.ecommerce.order.application;
import dev.pedro.learning.ecommerce.order.api.CreateOrderRequest;
import dev.pedro.learning.ecommerce.order.domain.*;
import dev.pedro.learning.ecommerce.order.event.*;
import dev.pedro.learning.ecommerce.order.outbox.OutboxWriter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;
import java.time.Instant;
import java.util.UUID;
@ApplicationScoped
public class OrderApplicationService {
    private static final Logger LOG=Logger.getLogger(OrderApplicationService.class);
    private final OrderRepository orders; private final OutboxWriter outbox;
    public OrderApplicationService(OrderRepository orders, OutboxWriter outbox){this.orders=orders;this.outbox=outbox;}
    @Transactional
    public OrderEntity create(CreateOrderRequest request, UUID correlationId) {
        MDC.put("correlationId", correlationId.toString());
        try {
            Instant now=Instant.now(); OrderEntity order=new OrderEntity(); order.id=UUID.randomUUID(); order.correlationId=correlationId;
            order.customerEmail=request.customerEmail(); order.productId=request.productId(); order.quantity=request.quantity(); order.unitPrice=request.unitPrice();
            order.totalAmount=request.unitPrice().multiply(java.math.BigDecimal.valueOf(request.quantity())); order.status=OrderStatus.PENDING; order.createdAt=now; order.updatedAt=now; orders.persist(order);
            EventEnvelope<OrderCreatedPayload> event=new EventEnvelope<>(UUID.randomUUID(), correlationId, "OrderCreated", now, 1,
                new OrderCreatedPayload(order.id,order.productId,order.quantity,order.unitPrice,order.totalAmount,order.customerEmail));
            outbox.append("order-events", order.id.toString(), event); LOG.info("[ORDER] OrderCreated"); return order;
        } finally { MDC.remove("correlationId"); }
    }
}
