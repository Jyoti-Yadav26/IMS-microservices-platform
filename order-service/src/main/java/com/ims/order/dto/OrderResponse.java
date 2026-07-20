package com.ims.order.dto;

import com.ims.order.entity.Order;
import com.ims.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        String orderNumber,
        String customerEmail,
        OrderStatus status,
        String failureReason,
        BigDecimal totalAmount,
        List<OrderItemResponse> items,
        Instant createdAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getOrderNumber(),
                order.getCustomerEmail(),
                order.getStatus(),
                order.getFailureReason(),
                order.getTotalAmount(),
                order.getItems().stream().map(OrderItemResponse::from).toList(),
                order.getCreatedAt()
        );
    }
}
