
package com.ecommerce.monolith.kafka.producer;

import com.ecommerce.monolith.kafka.config.KafkaTopicConfig;
import com.ecommerce.monolith.kafka.event.OrderPlacedEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class OrderEventProducer {

    private static final Logger log =
            LoggerFactory.getLogger(OrderEventProducer.class);

    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public OrderEventProducer(
            KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<SendResult<String, OrderPlacedEvent>>
    publishOrderPlacedEvent(OrderPlacedEvent event) {

        String key = String.valueOf(event.orderId());

        log.info("Publishing OrderPlacedEvent for orderId: {}", key);

        return kafkaTemplate.send(
                KafkaTopicConfig.ORDER_PLACED_TOPIC,
                key,
                event
        );
    }
}
