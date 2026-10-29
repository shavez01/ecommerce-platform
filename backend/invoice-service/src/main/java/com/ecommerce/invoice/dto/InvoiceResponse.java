package com.ecommerce.invoice.dto;

import java.math.BigDecimal;

public record InvoiceResponse(
        String invoiceId,
        String orderId,
        BigDecimal totalAmount,
        String status
) {}