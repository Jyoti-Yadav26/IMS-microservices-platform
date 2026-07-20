package com.ims.order.client.dto;

public record StockReservationResponse(boolean reserved, String sku, Integer remainingQuantity, String message) {
}
