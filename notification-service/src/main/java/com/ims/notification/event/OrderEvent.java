package com.ims.notification.event;

import java.time.Instant;

/** Mirrors order-service's published contract for the "order-events" topic. */
public record OrderEvent(
        String orderNumber,
        String customerEmail,
        String status,
        String reason,
        Instant occurredAt
) {
}
