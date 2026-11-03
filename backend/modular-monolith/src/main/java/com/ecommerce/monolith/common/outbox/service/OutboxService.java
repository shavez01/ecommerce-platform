
package com.ecommerce.monolith.common.outbox.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ecommerce.monolith.common.outbox.entity.OutboxEvent;
import com.ecommerce.monolith.common.outbox.repository.OutboxRepository;

@Service
public class OutboxService {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OutboxService(
            OutboxRepository outboxRepository,
            ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void saveEvent(
            String eventType,
            String aggregateId,
            String topic,
            Object event) {

        try {
            OutboxEvent outboxEvent = new OutboxEvent();

            outboxEvent.setEventType(eventType);
            outboxEvent.setAggregateId(aggregateId);
            outboxEvent.setTopic(topic);
            outboxEvent.setPayload(
                    objectMapper.writeValueAsString(event));

            outboxEvent.setStatus("PENDING");
            outboxEvent.setRetryCount(0);
            outboxEvent.setCreatedAt(LocalDateTime.now());

            outboxRepository.save(outboxEvent);

        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Failed to serialize outbox event", exception);
        }
    }
}
