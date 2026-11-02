package com.ecommerce.monolith.notification.consumer;

import com.ecommerce.monolith.kafka.config.KafkaTopicConfig;
import com.ecommerce.monolith.kafka.event.OrderPlacedEvent;
import com.ecommerce.monolith.notification.service.NotificationService;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderNotificationConsumer {

    private final NotificationService notificationService;

    public OrderNotificationConsumer(
            NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(
            topics = KafkaTopicConfig.ORDER_PLACED_TOPIC,
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeOrderPlacedEvent(OrderPlacedEvent event) {

        notificationService.processOrderPlacedEvent(event);
    }
}