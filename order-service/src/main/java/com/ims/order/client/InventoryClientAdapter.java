package com.ims.order.client;

import com.ims.order.client.dto.StockReservationRequest;
import com.ims.order.client.dto.StockReservationResponse;
import com.ims.order.exception.InventoryServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resilience boundary between order-service and inventory-service. Both annotations are
 * applied to the same "inventoryService" instance (configured in application.yml):
 * <ul>
 *   <li>{@code @Retry} first retries transient failures a bounded number of times with
 *       backoff (e.g. a single dropped packet or GC pause on the other side).</li>
 *   <li>{@code @CircuitBreaker} tracks the failure rate across calls; once it trips, calls
 *       fail fast into the fallback for a cool-down window instead of piling up load on a
 *       struggling downstream (and instead of every order request blocking for the full
 *       retry+timeout budget).</li>
 * </ul>
 * Business exceptions (insufficient stock / unknown SKU) are configured as
 * "ignoreExceptions" for both, so they propagate straight to the caller and are never
 * retried or counted as circuit-breaker failures.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryClientAdapter {

    private static final String RESILIENCE_INSTANCE = "inventoryService";

    private final InventoryClient inventoryClient;

    @CircuitBreaker(name = RESILIENCE_INSTANCE, fallbackMethod = "reserveStockFallback")
    @Retry(name = RESILIENCE_INSTANCE)
    public StockReservationResponse reserveStock(StockReservationRequest request) {
        return inventoryClient.reserveStock(request);
    }

    @CircuitBreaker(name = RESILIENCE_INSTANCE, fallbackMethod = "restockFallback")
    @Retry(name = RESILIENCE_INSTANCE)
    public void restock(String sku, int quantity) {
        inventoryClient.restock(sku, quantity);
    }

    @SuppressWarnings("unused") // invoked reflectively by Resilience4j
    private StockReservationResponse reserveStockFallback(StockReservationRequest request, Throwable t) {
        log.error("inventory-service unavailable while reserving sku={}: {}", request.sku(), t.toString());
        throw new InventoryServiceUnavailableException("inventory-service is currently unavailable", t);
    }

    @SuppressWarnings("unused") // invoked reflectively by Resilience4j
    private void restockFallback(String sku, int quantity, Throwable t) {
        // Compensation is best-effort: if inventory-service is down, the compensating
        // restock will simply fail too. We log loudly so an operator/reconciliation job
        // can fix the drift instead of silently losing stock.
        log.error("COMPENSATION FAILED: could not restock sku={} quantity={} after order failure: {}",
                sku, quantity, t.toString());
    }
}
