
package com.ecommerce.monolith.common.outbox.publisher;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ecommerce.monolith.common.outbox.entity.OutboxEvent;
import com.ecommerce.monolith.common.outbox.service.OutboxService;
import com.ecommerce.monolith.kafka.event.OrderPlacedEvent;
import com.ecommerce.monolith.kafka.event.PaymentFailedEvent;
import com.ecommerce.monolith.kafka.event.PaymentSuccessEvent;

@Component
public class OutboxPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxService outboxService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxPublisher(
            OutboxService outboxService,
            KafkaTemplate<String, Object> kafkaTemplate,
            ObjectMapper objectMapper) {

        this.outboxService = outboxService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {

        List<OutboxEvent> events =
                outboxService.claimPendingEvents();

        for (OutboxEvent event : events) {
            publishEvent(event);
        }
    }

    private void publishEvent(OutboxEvent event) {

        try {
            Object payload = deserializeEvent(event);

            kafkaTemplate.send(
                    event.getTopic(),
                    event.getAggregateId(),
                    payload
            ).get(10, TimeUnit.SECONDS);

            outboxService.markPublished(event.getId());

            log.info(
                    "Outbox event published. ID: {}, Type: {}",
                    event.getId(),
                    event.getEventType()
            );

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            outboxService.markFailed(
                    event.getId(), "Publishing interrupted");

            log.error("Outbox publishing interrupted. ID: {}",
                    event.getId(), exception);

        } catch (Exception exception) {

            outboxService.markFailed(
                    event.getId(), exception.getMessage());

            log.error("Outbox publishing failed. ID: {}",
                    event.getId(), exception);
        }
    }

    private Object deserializeEvent(OutboxEvent event)
            throws Exception {

        return switch (event.getEventType()) {

            case "ORDER_PLACED" ->
                    objectMapper.readValue(
                            event.getPayload(),
                            OrderPlacedEvent.class);

            case "PAYMENT_SUCCESS" ->
                    objectMapper.readValue(
                            event.getPayload(),
                            PaymentSuccessEvent.class);

            case "PAYMENT_FAILED" ->
                    objectMapper.readValue(
                            event.getPayload(),
                            PaymentFailedEvent.class);

            default -> throw new IllegalArgumentException(
                    "Unsupported event type: " + event.getEventType());
        };
    }
}
