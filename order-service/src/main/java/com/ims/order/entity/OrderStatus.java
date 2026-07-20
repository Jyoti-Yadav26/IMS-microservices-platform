package com.ims.order.entity;

public enum OrderStatus {
    /** Just created, stock reservation in progress. Transient - never returned as a final response. */
    PENDING,
    /** All items reserved successfully against inventory-service. */
    CONFIRMED,
    /** Inventory legitimately doesn't have enough stock - a business decision, not an error. */
    REJECTED,
    /** inventory-service was unreachable / circuit open / retries exhausted - an infra failure, safe to retry later. */
    FAILED
}
