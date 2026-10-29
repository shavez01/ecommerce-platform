package com.ecommerce.order.dto;

import java.math.BigDecimal;

public record OrderResponse(
        Long productId,
        String productName,
        int quantity,
        BigDecimal totalPrice,
        String status
) {}