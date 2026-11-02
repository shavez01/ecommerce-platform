
package com.ecommerce.monolith.kafka.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderPlacedEvent(
        Long orderId,
        Long customerId,
        BigDecimal totalAmount,
        LocalDateTime orderDate
) {
}
