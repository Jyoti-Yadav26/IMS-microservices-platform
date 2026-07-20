package com.ims.order.service;

import com.ims.order.dto.OrderRequest;
import com.ims.order.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(OrderRequest request);

    OrderResponse getOrder(String orderNumber);

    List<OrderResponse> getAllOrders();
}
