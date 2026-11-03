package com.ecommerce.monolith.payment.gateway;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Component
public class SimulatedPaymentGateway implements PaymentGateway {

    @Override
    @Retry(name = "paymentGateway")
    @CircuitBreaker(
            name = "paymentGateway",
            fallbackMethod = "chargeFallback"
    )
    public boolean charge(
            Long orderId,
            BigDecimal amount,
            String paymentMethod) {

        return true;
    }

    public boolean chargeFallback(
            Long orderId,
            BigDecimal amount,
            String paymentMethod,
            Throwable exception) {

        return false;
    }
}