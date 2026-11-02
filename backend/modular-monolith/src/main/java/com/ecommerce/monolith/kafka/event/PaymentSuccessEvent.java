
package com.ecommerce.monolith.kafka.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentSuccessEvent(
        Long paymentId,
        Long orderId,
        BigDecimal amount,
        String paymentMethod,
        String transactionReference,
        LocalDateTime paymentDate
) {
}
