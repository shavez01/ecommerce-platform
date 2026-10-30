
package com.ecommerce.monolith.invoice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class InvoiceRequest {

    @NotNull(message = "Order ID is required")
    @Positive(message = "Order ID must be positive")
    private Long orderId;

    public InvoiceRequest() {
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
}
