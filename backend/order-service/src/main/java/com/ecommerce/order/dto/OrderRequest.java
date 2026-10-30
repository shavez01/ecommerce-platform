package com.ecommerce.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;

public record OrderRequest(
        @NotBlank String productId,
        @NotNull @Positive Integer quantity
) {}