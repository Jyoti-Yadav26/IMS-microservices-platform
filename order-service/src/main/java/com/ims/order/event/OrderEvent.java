package com.ims.order.event;

import com.ims.order.entity.OrderStatus;

import java.time.Instant;

/**
 * Published once an order reaches a terminal state (CONFIRMED / REJECTED / FAILED).
 * notification-service consumes this asynchronously to email the customer - order-service
 * doesn't wait on, or even know about, that side effect, so a slow/down notification
 * pipeline never blocks or fails order placement.
 */
public record OrderEvent(
        String orderNumber,
        String customerEmail,
        OrderStatus status,
        String reason,
        Instant occurredAt
) {
}
