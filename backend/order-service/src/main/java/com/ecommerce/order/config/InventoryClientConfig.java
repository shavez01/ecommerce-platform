package com.ecommerce.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class InventoryClientConfig {

    @Bean
    RestClient inventoryRestClient(RestClient.Builder builder) {
        return builder.baseUrl("http://localhost:8084").build();
    }
}