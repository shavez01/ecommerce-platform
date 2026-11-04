package com.ecommerce.product.dto;

import java.math.BigDecimal;

public record ProductResponse(
        String id,
        String name,
        BigDecimal price,
        Integer stock
) {}