package com.ims.inventory.dto;

public record StockReservationResponse(
        boolean reserved,
        String sku,
        Integer remainingQuantity,
        String message
) {
}
