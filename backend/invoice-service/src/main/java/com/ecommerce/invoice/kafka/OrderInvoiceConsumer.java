package com.ecommerce.invoice.kafka;

import com.ecommerce.invoice.event.OrderConfirmedEvent;
import com.ecommerce.invoice.service.InvoiceService;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderInvoiceConsumer {

    private final InvoiceService invoiceService;

    public OrderInvoiceConsumer(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @KafkaListener(
        topics = "order.confirmed",
        groupId = "invoice-service"
    )
    public void consume(OrderConfirmedEvent event) {

        System.out.println(
                "Received order confirmation event: " + event.orderId()
        );

        invoiceService.generateInvoiceFromOrderEvent(event);
    }
}