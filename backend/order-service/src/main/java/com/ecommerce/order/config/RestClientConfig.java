package com.ecommerce.order.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    RestClient productRestClient(
            RestClient.Builder builder,
            @Value("${services.product.url}") String productUrl) {

        return builder.baseUrl(productUrl).build();
    }
}