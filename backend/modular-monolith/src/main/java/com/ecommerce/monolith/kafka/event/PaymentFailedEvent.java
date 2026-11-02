
package com.ecommerce.monolith.kafka.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentFailedEvent(
        Long paymentId,
        Long orderId,
        BigDecimal amount,
        String paymentMethod,
        String transactionReference,
        String failureReason,
        LocalDateTime paymentDate
) {
}
