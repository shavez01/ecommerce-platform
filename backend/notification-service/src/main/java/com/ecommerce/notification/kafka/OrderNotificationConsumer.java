package com.ecommerce.notification.kafka;

import com.ecommerce.notification.event.OrderConfirmedEvent;
import com.ecommerce.notification.entity.OrderNotification;
import com.ecommerce.notification.repository.OrderNotificationRepository;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrderNotificationConsumer {

    private final OrderNotificationRepository repository;

    public OrderNotificationConsumer(
            OrderNotificationRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(
        topics = "order.confirmed",
        groupId = "notification-service"
    )
    @Transactional
    public void consume(OrderConfirmedEvent event) {

        System.out.println(
                "Received OrderConfirmedEvent for order: "
                        + event.orderId()
        );

        // Ignore duplicate events
        if (repository.existsByOrderId(event.orderId())) {
            System.out.println(
                    "Notification already exists for order: "
                            + event.orderId()
            );
            return;
        }

        if (!"CONFIRMED".equals(event.status())) {
            System.out.println("Ignoring non-confirmed order event");
            return;
        }

        OrderNotification notification = OrderNotification.create(
                event.orderId(),
                event.productId(),
                event.productName(),
                event.quantity(),
                event.totalPrice(),
                event.createdAt()
        );

        repository.save(notification);

        System.out.println(
                "Order notification saved for order: "
                        + event.orderId()
        );
    }
}