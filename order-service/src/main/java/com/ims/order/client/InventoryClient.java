package com.ims.order.client;

import com.ims.order.client.dto.StockReservationRequest;
import com.ims.order.client.dto.StockReservationResponse;
import com.ims.order.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * "inventory-service" is a logical name resolved through Eureka by the Ribbon/Spring
 * Cloud LoadBalancer client baked into OpenFeign - no hard-coded host:port here, so
 * this keeps working as inventory-service scales to N instances or moves hosts.
 */
@FeignClient(name = "inventory-service", configuration = FeignClientConfig.class)
public interface InventoryClient {

    @PostMapping("/api/inventory/reserve")
    StockReservationResponse reserveStock(@RequestBody StockReservationRequest request);

    @PostMapping("/api/inventory/products/{sku}/restock")
    void restock(@PathVariable("sku") String sku, @RequestParam("quantity") int quantity);
}
