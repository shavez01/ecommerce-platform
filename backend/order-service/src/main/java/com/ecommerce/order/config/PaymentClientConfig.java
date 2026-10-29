package com.ecommerce.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class PaymentClientConfig {

    @Bean
    RestClient paymentRestClient(RestClient.Builder builder) {
        return builder.baseUrl("http://localhost:8085").build();
    }
}
