
package com.ecommerce.monolith.common.outbox.publisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ecommerce.monolith.common.outbox.entity.OutboxEvent;
import com.ecommerce.monolith.common.outbox.repository.OutboxRepository;
import com.ecommerce.monolith.kafka.event.OrderPlacedEvent;
import com.ecommerce.monolith.kafka.event.PaymentFailedEvent;
import com.ecommerce.monolith.kafka.event.PaymentSuccessEvent;

@Component
public class OutboxPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxPublisher(
            OutboxRepository outboxRepository,
            KafkaTemplate<String, Object> kafkaTemplate,
            ObjectMapper objectMapper) {

        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {

        List<OutboxEvent> events =
                outboxRepository.findTop50ByStatusOrderByCreatedAtAsc(
                        "PENDING");

        for (OutboxEvent event : events) {
            publishEvent(event);
        }
    }

    @Transactional
    public void publishEvent(OutboxEvent event) {

        try {
            Object payload = deserializeEvent(event);

            kafkaTemplate.send(
                    event.getTopic(),
                    event.getAggregateId(),
                    payload
            ).get(10, TimeUnit.SECONDS);

            event.setStatus("PUBLISHED");
            event.setPublishedAt(LocalDateTime.now());
            event.setLastError(null);

            outboxRepository.save(event);

            log.info("Outbox event published. ID: {}, Type: {}",
                    event.getId(), event.getEventType());

        } catch (Exception exception) {

            event.setRetryCount(event.getRetryCount() + 1);
            event.setLastError(exception.getMessage());

            outboxRepository.save(event);

            log.error("Outbox event publishing failed. ID: {}",
                    event.getId(), exception);
        }
    }

    private Object deserializeEvent(OutboxEvent event)
            throws Exception {

        return switch (event.getEventType()) {

            case "ORDER_PLACED" ->
                    objectMapper.readValue(
                            event.getPayload(), OrderPlacedEvent.class);

            case "PAYMENT_SUCCESS" ->
                    objectMapper.readValue(
                            event.getPayload(), PaymentSuccessEvent.class);

            case "PAYMENT_FAILED" ->
                    objectMapper.readValue(
                            event.getPayload(), PaymentFailedEvent.class);

            default -> throw new IllegalArgumentException(
                    "Unsupported event type: " + event.getEventType());
        };
    }
}
