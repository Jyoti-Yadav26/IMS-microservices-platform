package com.ims.order.dto;

import com.ims.order.entity.OrderItem;

import java.math.BigDecimal;

public record OrderItemResponse(String sku, Integer quantity, BigDecimal unitPrice, boolean reserved) {
    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(item.getSku(), item.getQuantity(), item.getUnitPrice(), item.isReserved());
    }
}
