package com.ims.inventory.exception;

/** Thrown when optimistic-lock retries are exhausted under heavy contention on one SKU. */
public class ConcurrentStockUpdateException extends RuntimeException {
    public ConcurrentStockUpdateException(String message) {
        super(message);
    }
}
