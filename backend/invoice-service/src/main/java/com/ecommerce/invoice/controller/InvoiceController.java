package com.ecommerce.invoice.controller;

import com.ecommerce.invoice.dto.InvoiceRequest;
import com.ecommerce.invoice.dto.InvoiceResponse;
import com.ecommerce.invoice.service.InvoiceService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    // Generate invoice
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceResponse generate(
            @Valid @RequestBody InvoiceRequest request) {

        return invoiceService.generateInvoice(request);
    }

    // Get invoice by invoice ID
    @GetMapping("/{invoiceId}")
    public InvoiceResponse getByInvoiceId(
            @PathVariable String invoiceId) {

        return invoiceService.getInvoiceByInvoiceId(invoiceId);
    }

    // Get invoice by order ID
    @GetMapping("/order/{orderId}")
    public InvoiceResponse getByOrderId(
            @PathVariable Long orderId) {

        return invoiceService.getInvoiceByOrderId(orderId);
    }
}