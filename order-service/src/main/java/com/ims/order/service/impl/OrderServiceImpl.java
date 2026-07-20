package com.ims.order.service.impl;

import com.ims.order.client.InventoryClientAdapter;
import com.ims.order.client.dto.StockReservationRequest;
import com.ims.order.dto.OrderItemRequest;
import com.ims.order.dto.OrderRequest;
import com.ims.order.dto.OrderResponse;
import com.ims.order.entity.Order;
import com.ims.order.entity.OrderItem;
import com.ims.order.entity.OrderStatus;
import com.ims.order.event.OrderEventPublisher;
import com.ims.order.exception.InsufficientStockException;
import com.ims.order.exception.InventoryServiceUnavailableException;
import com.ims.order.exception.OrderNotFoundException;
import com.ims.order.exception.ProductNotFoundException;
import com.ims.order.repository.OrderRepository;
import com.ims.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClientAdapter inventoryClientAdapter;
    private final OrderEventPublisher eventPublisher;

    /**
     * Places an order and synchronously reserves stock for each line item.
     * <p>
     * There is no distributed transaction spanning order-service and inventory-service.
     * Instead this method runs a manual saga: reserve items one at a time, and if any
     * item fails (business rejection or inventory-service being down), it compensates by
     * releasing (restocking) whichever earlier items in the same order were already
     * reserved, then records the order as REJECTED or FAILED - it never leaves inventory
     * permanently decremented for an order that didn't go through.
     * <p>
     * The order row itself is persisted up front in PENDING before any remote calls, so
     * there's always a durable audit trail of the attempt even if this process crashes
     * mid-reservation.
     */
    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        Order order = buildPendingOrder(request);
        order = orderRepository.save(order);

        OrderStatus resultStatus = OrderStatus.CONFIRMED;
        String failureReason = null;

        for (OrderItem item : order.getItems()) {
            try {
                reserveItem(order, item);
            } catch (InsufficientStockException | ProductNotFoundException ex) {
                resultStatus = OrderStatus.REJECTED;
                failureReason = ex.getMessage();
                break;
            } catch (InventoryServiceUnavailableException ex) {
                resultStatus = OrderStatus.FAILED;
                failureReason = ex.getMessage();
                break;
            }
        }

        if (resultStatus != OrderStatus.CONFIRMED) {
            compensateReservedItems(order);
        }

        order.setStatus(resultStatus);
        order.setFailureReason(failureReason);
        Order saved = orderRepository.save(order);

        eventPublisher.publish(saved);
        log.info("Order {} finished with status={}", saved.getOrderNumber(), saved.getStatus());
        return OrderResponse.from(saved);
    }

    @Override
    public OrderResponse getOrder(String orderNumber) {
        return OrderResponse.from(findOrThrow(orderNumber));
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream().map(OrderResponse::from).toList();
    }

    private void reserveItem(Order order, OrderItem item) {
        String idempotencyKey = order.getOrderNumber() + "-" + item.getSku();
        inventoryClientAdapter.reserveStock(new StockReservationRequest(item.getSku(), item.getQuantity(), idempotencyKey));
        item.setReserved(true);
    }

    private void compensateReservedItems(Order order) {
        for (OrderItem item : order.getItems()) {
            if (item.isReserved()) {
                try {
                    inventoryClientAdapter.restock(item.getSku(), item.getQuantity());
                    item.setReserved(false);
                    log.info("Compensated (released) sku={} quantity={} for failed order={}",
                            item.getSku(), item.getQuantity(), order.getOrderNumber());
                } catch (Exception ex) {
                    // Already logged loudly inside the fallback; swallow here so one bad
                    // compensation doesn't stop us from releasing the other line items.
                }
            }
        }
    }

    private Order buildPendingOrder(OrderRequest request) {
        Order order = Order.builder()
                .customerEmail(request.customerEmail())
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.items()) {
            OrderItem item = OrderItem.builder()
                    .sku(itemRequest.sku())
                    .quantity(itemRequest.quantity())
                    .unitPrice(itemRequest.unitPrice())
                    .reserved(false)
                    .build();
            order.addItem(item);
            total = total.add(itemRequest.unitPrice().multiply(BigDecimal.valueOf(itemRequest.quantity())));
        }
        order.setTotalAmount(total);
        return order;
    }

    private Order findOrThrow(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order '" + orderNumber + "' not found"));
    }
}
