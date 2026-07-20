package com.ims.order.config;

import feign.Request;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

/**
 * Per-client Feign configuration for inventory-service calls. Explicit, tight timeouts
 * are the first line of resilience: a slow downstream should fail fast rather than tie
 * up caller threads indefinitely, giving the retry/circuit-breaker layer a chance to act.
 */
public class FeignClientConfig {

    @Bean
    public Request.Options options() {
        return new Request.Options(
                2_000, TimeUnit.MILLISECONDS,  // connect timeout
                3_000, TimeUnit.MILLISECONDS,  // read timeout
                true);
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new InventoryFeignErrorDecoder();
    }
}
