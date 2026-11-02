
package com.ecommerce.monolith.notification.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.ecommerce.monolith.customer.dto.CustomerResponse;
import com.ecommerce.monolith.customer.service.CustomerService;
import com.ecommerce.monolith.kafka.config.KafkaTopicConfig;
import com.ecommerce.monolith.kafka.event.PaymentFailedEvent;
import com.ecommerce.monolith.notification.service.NotificationService;
import com.ecommerce.monolith.order.entity.Order;
import com.ecommerce.monolith.order.service.OrderService;

@Component
public class PaymentFailedConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentFailedConsumer.class);

    private final OrderService orderService;
    private final CustomerService customerService;
    private final NotificationService notificationService;

    public PaymentFailedConsumer(
            OrderService orderService,
            CustomerService customerService,
            NotificationService notificationService) {

        this.orderService = orderService;
        this.customerService = customerService;
        this.notificationService = notificationService;
    }

    @KafkaListener(
        topics = KafkaTopicConfig.PAYMENT_FAILED_TOPIC,
        groupId = "payment-failed-notification-group",
        containerFactory = "paymentFailedKafkaListenerContainerFactory",
        properties = {
            "spring.json.value.default.type=com.ecommerce.monolith.kafka.event.PaymentFailedEvent"
        }
    )


    public void consumePaymentFailed(PaymentFailedEvent event) {

        log.info("Payment failure event received for order: {}", event.orderId());

        Order order = orderService.getOrderById(event.orderId());

        CustomerResponse customer =
                customerService.getCustomerById(order.getCustomerId());

        notificationService.recordNotification(
                event.orderId(),
                "PAYMENT_FAILED",
                customer.getEmail(),
                "Payment Failed",
                "Payment for order #" + event.orderId()
                        + " failed. Reason: " + event.failureReason()
        );

        log.info("Payment failure notification recorded for order: {}",
                event.orderId());
    }
}
