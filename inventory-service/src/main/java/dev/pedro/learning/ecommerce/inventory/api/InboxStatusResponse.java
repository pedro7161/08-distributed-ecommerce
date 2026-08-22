package dev.pedro.learning.ecommerce.inventory.api;

public record InboxStatusResponse(boolean processed, String eventType) {}
