package com.ecommerce.invoice.service;

import com.ecommerce.invoice.dto.InvoiceRequest;
import com.ecommerce.invoice.dto.InvoiceResponse;
import com.ecommerce.invoice.entity.Invoice;
import com.ecommerce.invoice.event.OrderConfirmedEvent;
import com.ecommerce.invoice.repository.InvoiceRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    // Generate invoice
    @Transactional
    public InvoiceResponse generateInvoice(InvoiceRequest request) {

        invoiceRepository.findByOrderId(request.orderId())
                .ifPresent(invoice -> {
                    throw new IllegalStateException(
                            "Invoice already exists for order: "
                                    + request.orderId()
                    );
                });

        Invoice invoice = Invoice.create(
                request.orderId(),
                request.productName(),
                request.quantity(),
                request.totalAmount()
        );

        Invoice savedInvoice = invoiceRepository.save(invoice);

        return mapToResponse(savedInvoice);
    }

    // Retrieve invoice by invoice ID
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByInvoiceId(String invoiceId) {

        Invoice invoice = invoiceRepository.findByInvoiceId(invoiceId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Invoice not found: " + invoiceId
                ));

        return mapToResponse(invoice);
    }

    // Retrieve invoice by order ID
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByOrderId(Long orderId) {

        Invoice invoice = invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Invoice not found for order: " + orderId
                ));

        return mapToResponse(invoice);
    }

    @Transactional
    public void generateInvoiceFromOrderEvent(OrderConfirmedEvent event) {

        // Process only confirmed orders
        if (!"CONFIRMED".equals(event.status())) {
            return;
        }

        // Prevent duplicate invoice generation
        if (invoiceRepository.findByOrderId(event.orderId()).isPresent()) {
            System.out.println(
                    "Invoice already exists for order: " + event.orderId()
            );
            return;
        }

        Invoice invoice = Invoice.create(
                event.orderId(),
                event.productName(),
                event.quantity(),
                event.totalPrice()
        );

        invoiceRepository.save(invoice);

        System.out.println(
                "Invoice generated successfully for order: " + event.orderId()
        );
    }

    private InvoiceResponse mapToResponse(Invoice invoice) {

        return new InvoiceResponse(
                invoice.getInvoiceId(),
                invoice.getOrderId().toString(),
                invoice.getTotalAmount(),
                invoice.getStatus().name()
        );
    }
}