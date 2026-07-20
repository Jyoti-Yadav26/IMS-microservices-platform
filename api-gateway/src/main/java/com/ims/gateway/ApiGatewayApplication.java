package com.ims.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Single public entry point for all IMS clients. Routes are resolved dynamically
 * via Eureka service discovery (see application.yml "lb://<service-name>" URIs),
 * so services can scale horizontally / move hosts without gateway config changes.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
