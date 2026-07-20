package com.ims.order.exception;

/**
 * Raised by {@link com.ims.order.client.InventoryClientAdapter}'s Resilience4j fallback
 * when inventory-service could not be reached after retries, or the circuit is open.
 * This is an infrastructure failure (as opposed to a business rejection) and is treated
 * as retryable by the caller.
 */
public class InventoryServiceUnavailableException extends RuntimeException {
    public InventoryServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
