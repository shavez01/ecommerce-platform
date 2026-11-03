package com.ecommerce.monolith.common.outbox.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.monolith.common.outbox.entity.OutboxEvent;
import com.ecommerce.monolith.common.outbox.repository.OutboxRepository;

@ExtendWith(MockitoExtension.class)
class OutboxServiceTest {

    @Mock OutboxRepository outboxRepository;
    private OutboxService service;

    @BeforeEach
    void setUp() {
        service = new OutboxService(outboxRepository, new ObjectMapper());
    }

    @Test
    void saveEventSerializesPayloadAndStoresPendingEvent() {
        service.saveEvent("TEST_EVENT", "123", "test-topic", new TestPayload("hello"));

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxRepository).save(captor.capture());
        OutboxEvent event = captor.getValue();

        assertEquals("TEST_EVENT", event.getEventType());
        assertEquals("123", event.getAggregateId());
        assertEquals("test-topic", event.getTopic());
        assertEquals("PENDING", event.getStatus());
        assertEquals(0, event.getRetryCount());
        assertTrue(event.getPayload().contains("hello"));
    }

    @Test
    void claimPendingEventsMarksEventsProcessingAndSetsLease() {
        OutboxEvent event = new OutboxEvent();
        event.setStatus("PENDING");
        event.setRetryCount(0);
        when(outboxRepository.claimPendingEvents(any(LocalDateTime.class)))
                .thenReturn(List.of(event));
        when(outboxRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<OutboxEvent> result = service.claimPendingEvents();

        assertEquals(1, result.size());
        assertEquals("PROCESSING", event.getStatus());
        assertNotNull(event.getLockedUntil());
    }

    @Test
    void markFailedIncrementsRetryAndReturnsEventToPending() {
        OutboxEvent event = new OutboxEvent();
        event.setRetryCount(1);
        event.setStatus("PROCESSING");
        when(outboxRepository.findById(1L)).thenReturn(Optional.of(event));

        service.markFailed(1L, "Kafka unavailable");

        assertEquals(2, event.getRetryCount());
        assertEquals("PENDING", event.getStatus());
        assertEquals("Kafka unavailable", event.getLastError());
        assertNull(event.getLockedUntil());
    }

    @Test
    void fifthFailureMovesEventToFailed() {
        OutboxEvent event = new OutboxEvent();
        event.setRetryCount(4);
        event.setStatus("PROCESSING");
        when(outboxRepository.findById(1L)).thenReturn(Optional.of(event));

        service.markFailed(1L, "Send failed");

        assertEquals(5, event.getRetryCount());
        assertEquals("FAILED", event.getStatus());
    }

    @Test
    void markPublishedSetsPublishedStatusAndTimestamp() {
        OutboxEvent event = new OutboxEvent();
        event.setStatus("PROCESSING");
        event.setLockedUntil(LocalDateTime.now().plusMinutes(2));
        event.setLastError("old error");
        when(outboxRepository.findById(1L)).thenReturn(Optional.of(event));

        service.markPublished(1L);

        assertEquals("PUBLISHED", event.getStatus());
        assertNotNull(event.getPublishedAt());
        assertNull(event.getLockedUntil());
        assertNull(event.getLastError());
    }

    record TestPayload(String message) {}
}
