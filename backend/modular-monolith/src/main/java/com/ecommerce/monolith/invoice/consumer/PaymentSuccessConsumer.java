
package com.ecommerce.monolith.invoice.consumer;

import com.ecommerce.monolith.invoice.dto.InvoiceRequest;
import com.ecommerce.monolith.invoice.service.InvoiceService;
import com.ecommerce.monolith.kafka.config.KafkaTopicConfig;
import com.ecommerce.monolith.kafka.event.PaymentSuccessEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentSuccessConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentSuccessConsumer.class);

    private final InvoiceService invoiceService;

    public PaymentSuccessConsumer(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @KafkaListener(
        topics = KafkaTopicConfig.PAYMENT_SUCCESS_TOPIC,
        groupId = "invoice-group",
        properties = {
            "spring.json.value.default.type=com.ecommerce.monolith.kafka.event.PaymentSuccessEvent"
        }
    )
    public void consumePaymentSuccess(PaymentSuccessEvent event) {

        log.info("Payment success event received for Order ID: {}",
                event.orderId());

        InvoiceRequest request = new InvoiceRequest();
        request.setOrderId(event.orderId());

        invoiceService.generateInvoice(request);

        log.info("Invoice generated successfully for Order ID: {}",
                event.orderId());
    }
}
