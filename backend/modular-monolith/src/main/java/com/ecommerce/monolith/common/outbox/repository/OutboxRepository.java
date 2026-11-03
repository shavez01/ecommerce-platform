
package com.ecommerce.monolith.common.outbox.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecommerce.monolith.common.outbox.entity.OutboxEvent;

public interface OutboxRepository
        extends JpaRepository<OutboxEvent, Long> {

    @Query(value = """
        SELECT *
        FROM outbox_events
        WHERE status = 'PENDING'
           OR (status = 'PROCESSING' AND locked_until < :now)
        ORDER BY created_at ASC
        LIMIT 50
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    List<OutboxEvent> claimPendingEvents(
            @Param("now") LocalDateTime now);
}
