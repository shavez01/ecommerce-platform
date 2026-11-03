package com.ecommerce.order.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class PaymentClientConfig {

    @Bean
    RestClient paymentRestClient(
            RestClient.Builder builder,
            @Value("${services.payment.url}") String paymentUrl) {

        return builder.baseUrl(paymentUrl).build();
    }
}