package dev.pedro.learning.ecommerce.order.api;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;
public record CreateOrderRequest(@Email @NotBlank String customerEmail, @NotNull UUID productId, @Min(1) int quantity, @NotNull @DecimalMin("0.01") BigDecimal unitPrice) {}
