
package com.ecommerce.monolith.invoice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.monolith.invoice.dto.InvoiceRequest;
import com.ecommerce.monolith.invoice.entity.Invoice;
import com.ecommerce.monolith.invoice.service.InvoiceService;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    // Generate invoice
    @PostMapping
    public ResponseEntity<Invoice> generateInvoice(
            @RequestBody InvoiceRequest request) {

        Invoice invoice = invoiceService.generateInvoice(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invoice);
    }

    // Get invoice by ID
    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getInvoiceById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                invoiceService.getInvoiceById(id));
    }

    // Get invoice by Order ID
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Invoice> getInvoiceByOrderId(
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                invoiceService.getInvoiceByOrderId(orderId));
    }

    // Get all invoices
    @GetMapping
    public ResponseEntity<List<Invoice>> getAllInvoices() {

        return ResponseEntity.ok(
                invoiceService.getAllInvoices());
    }

    // Health check
    @GetMapping("/health")
    public String health() {
        return "Invoice module is running";
    }
}
