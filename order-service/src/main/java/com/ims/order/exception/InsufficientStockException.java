package com.ims.order.exception;

/** Inventory-service legitimately doesn't have enough stock - not retried, not a circuit-breaker failure. */
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
