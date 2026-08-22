package dev.pedro.learning.ecommerce.inventory.application;
import dev.pedro.learning.ecommerce.inventory.domain.*; import dev.pedro.learning.ecommerce.inventory.event.*; import dev.pedro.learning.ecommerce.inventory.inbox.*; import dev.pedro.learning.ecommerce.inventory.outbox.OutboxWriter; import jakarta.enterprise.context.ApplicationScoped; import jakarta.persistence.LockModeType; import jakarta.transaction.Transactional; import org.jboss.logging.Logger; import org.jboss.logging.MDC; import java.time.Instant; import java.util.UUID;
@ApplicationScoped public class InventorySagaService {
 private static final Logger LOG=Logger.getLogger(InventorySagaService.class); private final InventoryItemRepository items; private final InventoryReservationRepository reservations; private final InboxEventRepository inbox; private final OutboxWriter outbox;
 public InventorySagaService(InventoryItemRepository items,InventoryReservationRepository reservations,InboxEventRepository inbox,OutboxWriter outbox){this.items=items;this.reservations=reservations;this.inbox=inbox;this.outbox=outbox;}
 @Transactional public void onOrderCreated(EventEnvelope<OrderCreatedPayload> event){
  if(begin(event))return; try { var p=event.payload(); InventoryItem item=items.findById(p.productId(),LockModeType.PESSIMISTIC_WRITE);
   if(item==null||item.availableQuantity<p.quantity()){
    var failed=new EventEnvelope<>(UUID.randomUUID(),event.correlationId(),"StockReservationFailed",Instant.now(),1,new StockReservationFailedPayload(p.orderId(),p.productId(),p.quantity(),"Insufficient inventory")); outbox.append("inventory-events",p.orderId().toString(),failed); finish(event); LOG.info("[INVENTORY] StockReservationFailed"); return; }
   item.availableQuantity-=p.quantity(); item.reservedQuantity+=p.quantity(); InventoryReservation r=new InventoryReservation();r.id=UUID.randomUUID();r.orderId=p.orderId();r.productId=p.productId();r.quantity=p.quantity();r.status=ReservationStatus.RESERVED;r.createdAt=Instant.now();reservations.persist(r);
   var reserved=new EventEnvelope<>(UUID.randomUUID(),event.correlationId(),"StockReserved",Instant.now(),1,new StockReservedPayload(p.orderId(),p.productId(),p.quantity(),p.totalAmount(),p.customerEmail()));outbox.append("inventory-events",p.orderId().toString(),reserved);finish(event);LOG.info("[INVENTORY] StockReserved");
  } finally {MDC.remove("correlationId");}
 }
 @Transactional public void onPaymentFailed(EventEnvelope<PaymentFailedPayload> event){
  if(begin(event))return; try { InventoryReservation r=reservations.findByOrderId(event.payload().orderId()); if(r==null){finish(event);LOG.info("[INVENTORY] No reservation to compensate");return;} if(r.status==ReservationStatus.RELEASED){finish(event);LOG.info("[INVENTORY] Reservation already released");return;}
   InventoryItem item=items.findById(r.productId,LockModeType.PESSIMISTIC_WRITE); item.availableQuantity+=r.quantity; item.reservedQuantity-=r.quantity; r.status=ReservationStatus.RELEASED;r.releasedAt=Instant.now();
   var released=new EventEnvelope<>(UUID.randomUUID(),event.correlationId(),"StockReleased",Instant.now(),1,new StockReleasedPayload(r.orderId,r.productId,r.quantity)); outbox.append("inventory-events",r.orderId.toString(),released);finish(event);LOG.info("[INVENTORY] StockReleased");
  } finally {MDC.remove("correlationId");}
 }
 private boolean begin(EventEnvelope<?> e){MDC.put("correlationId",e.correlationId().toString()); if(inbox.findById(e.eventId())!=null){LOG.infof("[INVENTORY] Duplicate %s ignored",e.eventType());MDC.remove("correlationId");return true;} InboxEvent row=new InboxEvent();row.eventId=e.eventId();row.eventType=e.eventType();row.receivedAt=Instant.now();inbox.persist(row);return false;}
 private void finish(EventEnvelope<?> e){InboxEvent row=inbox.findById(e.eventId());if(row!=null)row.processedAt=Instant.now();}
}
