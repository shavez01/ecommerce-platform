package com.ecommerce.order.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderConfirmedEvent(
        Long orderId,
        String productId,
        String productName,
        Integer quantity,
        BigDecimal totalPrice,
        String status,
        LocalDateTime createdAt
) {
}