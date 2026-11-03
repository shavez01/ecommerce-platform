
package com.ecommerce.monolith.common.outbox.service;

import java.util.List;
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

    
    @Transactional
    public List<OutboxEvent> claimPendingEvents() {

        LocalDateTime now = LocalDateTime.now();

        List<OutboxEvent> events =
                outboxRepository.claimPendingEvents(now);

        for (OutboxEvent event : events) {
            event.setStatus("PROCESSING");
            event.setLockedUntil(now.plusMinutes(2));
        }

        return outboxRepository.saveAll(events);
    }

    @Transactional
    public void markPublished(Long eventId) {

        OutboxEvent event = outboxRepository.findById(eventId)
                .orElseThrow(() ->
                        new IllegalStateException("Outbox event not found: " + eventId));

        event.setStatus("PUBLISHED");
        event.setPublishedAt(LocalDateTime.now());
        event.setLockedUntil(null);
        event.setLastError(null);
    }

    @Transactional
    public void markFailed(Long eventId, String errorMessage) {

        OutboxEvent event = outboxRepository.findById(eventId)
                .orElseThrow(() ->
                        new IllegalStateException("Outbox event not found: " + eventId));

        int retryCount = event.getRetryCount() + 1;

        event.setRetryCount(retryCount);
        event.setLastError(
                errorMessage == null ? "Unknown publishing error"
                        : errorMessage.substring(
                                0, Math.min(errorMessage.length(), 1000)));

        event.setLockedUntil(null);

        if (retryCount >= 5) {
            event.setStatus("FAILED");
        } else {
            event.setStatus("PENDING");
        }
    }

}
