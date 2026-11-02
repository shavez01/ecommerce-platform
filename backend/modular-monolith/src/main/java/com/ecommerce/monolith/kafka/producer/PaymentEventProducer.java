
package com.ecommerce.monolith.kafka.producer;

import com.ecommerce.monolith.kafka.config.KafkaTopicConfig;
import com.ecommerce.monolith.kafka.event.PaymentSuccessEvent;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventProducer {

    private final KafkaTemplate<String, PaymentSuccessEvent> kafkaTemplate;

    public PaymentEventProducer(
            KafkaTemplate<String, PaymentSuccessEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPaymentSuccess(PaymentSuccessEvent event) {

        kafkaTemplate.send(
                KafkaTopicConfig.PAYMENT_SUCCESS_TOPIC,
                String.valueOf(event.orderId()),
                event
        );
    }
}
