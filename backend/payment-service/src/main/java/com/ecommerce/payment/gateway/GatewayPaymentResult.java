package com.ecommerce.payment.gateway;

public record GatewayPaymentResult(
        boolean approved,
        String transactionReference,
        String failureReason
) {
}