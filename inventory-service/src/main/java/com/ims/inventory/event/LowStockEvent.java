package com.ims.inventory.event;

import java.time.Instant;

/**
 * Published to Kafka whenever a reservation brings a product's quantity at or below
 * its reorder threshold. This is fire-and-forget / eventually-consistent by design:
 * inventory-service doesn't need (or want) to know who is listening. notification-service
 * happens to consume it today to alert an admin, but any number of future consumers
 * (e.g. an auto-reordering service) could subscribe without inventory-service changing.
 */
public record LowStockEvent(
        String sku,
        String productName,
        Integer remainingQuantity,
        Integer reorderThreshold,
        Instant occurredAt
) {
}
