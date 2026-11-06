package com.ecommerce.payment.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentGatewaySimulator {

    @Value("${app.payment.simulator.result:SUCCESS}")
    private String simulatedResult;

    public GatewayPaymentResult authorize() {

        if ("SUCCESS".equalsIgnoreCase(simulatedResult)) {

            return new GatewayPaymentResult(
                    true,
                    "TXN-" + UUID.randomUUID(),
                    null
            );
        }

        if ("FAILED".equalsIgnoreCase(simulatedResult)) {

            return new GatewayPaymentResult(
                    false,
                    null,
                    "Payment declined by simulated gateway"
            );
        }

        throw new IllegalStateException(
                "Invalid payment simulator result: " + simulatedResult
        );
    }
}