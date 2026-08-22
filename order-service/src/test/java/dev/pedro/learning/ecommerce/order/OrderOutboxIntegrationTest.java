package dev.pedro.learning.ecommerce.order;
import dev.pedro.learning.ecommerce.order.outbox.OutboxEventRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
@QuarkusTest
class OrderOutboxIntegrationTest {
    @Inject OutboxEventRepository outbox;
    @Test void creatingOrderWritesOrderAndOutboxTogether(){
        UUID correlation=UUID.randomUUID();
        given().contentType(ContentType.JSON).header("X-Correlation-ID",correlation.toString())
          .body("{\"customerEmail\":\"learner@example.com\",\"productId\":\"11111111-1111-1111-1111-111111111111\",\"quantity\":2,\"unitPrice\":12.50}")
          .when().post("/orders").then().statusCode(201).header("X-Correlation-ID",equalTo(correlation.toString())).body("status",equalTo("PENDING"));
        assertEquals(1, outbox.count("type", "OrderCreated"));
    }
}
