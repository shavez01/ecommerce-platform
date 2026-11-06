package com.ecommerce.order.kafka;

import com.ecommerce.order.event.OrderConfirmedEvent;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventProducer {

    private static final String TOPIC = "order.confirmed";

    private final KafkaTemplate<String, OrderConfirmedEvent> kafkaTemplate;

    public OrderEventProducer(
            KafkaTemplate<String, OrderConfirmedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderConfirmed(OrderConfirmedEvent event) {

        kafkaTemplate.send(
                TOPIC,
                event.orderId().toString(),
                event
        );
    }
}