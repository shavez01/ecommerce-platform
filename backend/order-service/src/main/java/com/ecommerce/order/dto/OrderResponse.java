package com.ecommerce.order.dto;

import java.math.BigDecimal;

public record OrderResponse(
        Long orderId,
        String productId,
        String productName,
        Integer quantity,
        BigDecimal totalPrice,
        String status
) {}