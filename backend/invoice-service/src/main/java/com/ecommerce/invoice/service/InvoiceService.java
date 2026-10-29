package com.ecommerce.invoice.service;

import com.ecommerce.invoice.dto.InvoiceRequest;
import com.ecommerce.invoice.dto.InvoiceResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class InvoiceService {

    public InvoiceResponse generateInvoice(InvoiceRequest request) {
        return new InvoiceResponse(
                "INV-" + UUID.randomUUID(),
                request.orderId(),
                request.totalAmount(),
                "GENERATED"
        );
    }
}