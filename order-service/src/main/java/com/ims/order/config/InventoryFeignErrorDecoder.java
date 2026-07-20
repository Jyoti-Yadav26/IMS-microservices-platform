package com.ims.order.config;

import com.ims.order.exception.InsufficientStockException;
import com.ims.order.exception.ProductNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Translates inventory-service's HTTP error responses into specific local exceptions so
 * that Resilience4j can be configured to treat business rejections (404/409) differently
 * from real infrastructure failures (5xx/timeouts): only the latter should be retried or
 * count against the circuit breaker.
 */
public class InventoryFeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new ErrorDecoder.Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        String body = readBody(response);
        if (response.status() == 409) {
            return new InsufficientStockException(body != null ? body : "Insufficient stock");
        }
        if (response.status() == 404) {
            return new ProductNotFoundException(body != null ? body : "Product not found");
        }
        // 5xx, connection resets etc. fall through to Feign's default handling, which
        // surfaces as a retryable exception to the circuit breaker / retry layer.
        return defaultDecoder.decode(methodKey, response);
    }

    private String readBody(Response response) {
        if (response.body() == null) {
            return null;
        }
        try (var is = response.body().asInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }
}
