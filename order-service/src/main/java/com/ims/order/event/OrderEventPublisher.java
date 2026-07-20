package com.ims.order.event;

import com.ims.order.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    public static final String ORDER_EVENTS_TOPIC = "order-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(Order order) {
        OrderEvent event = new OrderEvent(
                order.getOrderNumber(),
                order.getCustomerEmail(),
                order.getStatus(),
                order.getFailureReason(),
                Instant.now()
        );
        kafkaTemplate.send(ORDER_EVENTS_TOPIC, order.getOrderNumber(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish OrderEvent for order={}", order.getOrderNumber(), ex);
                    } else {
                        log.info("Published OrderEvent order={} status={}", order.getOrderNumber(), order.getStatus());
                    }
                });
    }
}
