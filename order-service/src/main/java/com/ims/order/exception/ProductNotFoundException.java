package com.ims.order.exception;

/** The referenced SKU doesn't exist in inventory-service - a client input error, never retried. */
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String message) {
        super(message);
    }
}
