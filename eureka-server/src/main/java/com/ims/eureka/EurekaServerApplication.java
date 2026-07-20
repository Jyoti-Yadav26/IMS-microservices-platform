package com.ims.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Service Registry for the IMS microservices.
 * Every service (inventory, order, notification, gateway) registers itself here
 * on startup so that clients can discover instances by logical name instead of
 * hard-coded host:port, and the gateway/Feign clients can client-side load-balance.
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
