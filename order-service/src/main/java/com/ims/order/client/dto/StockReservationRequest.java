package com.ims.order.client.dto;

/**
 * Mirrors inventory-service's request contract. Deliberately duplicated rather than
 * shared via a common library: each service owns its view of the contract so the two
 * teams/services can evolve independently, at the cost of keeping them manually in sync.
 */
public record StockReservationRequest(String sku, Integer quantity, String idempotencyKey) {
}
