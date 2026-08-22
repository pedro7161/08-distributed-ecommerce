package dev.pedro.learning.ecommerce.order.api;
import dev.pedro.learning.ecommerce.order.application.OrderApplicationService;
import dev.pedro.learning.ecommerce.order.domain.OrderRepository;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.net.URI;
import java.util.UUID;
@Path("/orders") @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
public class OrderResource {
    private final OrderApplicationService service; private final OrderRepository orders;
    public OrderResource(OrderApplicationService service, OrderRepository orders){this.service=service;this.orders=orders;}
    @POST public Response create(@Valid CreateOrderRequest request, @HeaderParam("X-Correlation-ID") String correlationHeader){
        UUID correlationId=resolveCorrelationId(correlationHeader); var order=service.create(request,correlationId);
        return Response.created(URI.create("/orders/"+order.id)).header("X-Correlation-ID",correlationId).entity(OrderResponse.from(order)).build();
    }
    @GET @Path("/{id}") public OrderResponse get(@PathParam("id") UUID id){var order=orders.findById(id);if(order==null)throw new NotFoundException();return OrderResponse.from(order);}
    private UUID resolveCorrelationId(String value){if(value==null||value.isBlank())return UUID.randomUUID();try{return UUID.fromString(value);}catch(IllegalArgumentException e){return UUID.nameUUIDFromBytes(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));}}
}
