package com.ecommerce.invoice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record InvoiceRequest(
        @NotBlank String orderId,
        @NotBlank String productName,
        @Positive int quantity,
        @NotNull @Positive BigDecimal totalAmount
) {}