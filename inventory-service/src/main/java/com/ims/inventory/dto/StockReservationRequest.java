package com.ims.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Sent by order-service (synchronous REST call) when it wants to reserve/decrement
 * stock for an order. {@code idempotencyKey} lets inventory-service safely ignore a
 * duplicate reservation caused by an order-service retry after a network timeout.
 */
public record StockReservationRequest(
        @NotBlank(message = "sku is required") String sku,
        @NotNull @Min(value = 1, message = "quantity must be >= 1") Integer quantity,
        @NotBlank(message = "idempotencyKey is required") String idempotencyKey
) {
}
