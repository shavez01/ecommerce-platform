package com.ecommerce.invoice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record InvoiceRequest(
        @NotNull @Positive Long orderId,
        @NotBlank String productName,
        @NotNull @Positive Integer quantity,
        @NotNull @Positive BigDecimal totalAmount
) {}