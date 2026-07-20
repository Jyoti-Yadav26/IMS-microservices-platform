package com.ims.notification.event;

import java.time.Instant;

/** Mirrors inventory-service's published contract for the "low-stock-events" topic. */
public record LowStockEvent(
        String sku,
        String productName,
        Integer remainingQuantity,
        Integer reorderThreshold,
        Instant occurredAt
) {
}
