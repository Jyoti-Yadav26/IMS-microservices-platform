package com.ims.inventory.event;

import com.ims.inventory.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventPublisher {

    public static final String LOW_STOCK_TOPIC = "low-stock-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishLowStock(Product product) {
        LowStockEvent event = new LowStockEvent(
                product.getSku(),
                product.getName(),
                product.getQuantity(),
                product.getReorderThreshold(),
                Instant.now()
        );
        // Key by SKU so all events for the same product land on the same partition and
        // are processed in order by a single consumer instance.
        kafkaTemplate.send(LOW_STOCK_TOPIC, product.getSku(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish LowStockEvent for sku={}", product.getSku(), ex);
                    } else {
                        log.info("Published LowStockEvent for sku={} remainingQuantity={}", product.getSku(), product.getQuantity());
                    }
                });
    }
}
