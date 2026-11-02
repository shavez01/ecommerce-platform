
package com.ecommerce.monolith.kafka.producer;

import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.ecommerce.monolith.kafka.config.KafkaTopicConfig;
import com.ecommerce.monolith.kafka.event.PaymentSuccessEvent;
import com.ecommerce.monolith.kafka.event.PaymentFailedEvent;

@Service
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentEventProducer(
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<SendResult<String, Object>>
            publishPaymentSuccess(PaymentSuccessEvent event) {

        return kafkaTemplate.send(
                KafkaTopicConfig.PAYMENT_SUCCESS_TOPIC,
                String.valueOf(event.orderId()),
                event
        );
    }

    public CompletableFuture<SendResult<String, Object>>
            publishPaymentFailed(PaymentFailedEvent event) {

        return kafkaTemplate.send(
                KafkaTopicConfig.PAYMENT_FAILED_TOPIC,
                String.valueOf(event.orderId()),
                event
        );
    }
}
