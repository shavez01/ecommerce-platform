
package com.ecommerce.monolith.invoice.dto;

public class InvoiceRequest {

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
