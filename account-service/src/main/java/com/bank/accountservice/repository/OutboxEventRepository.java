package com.bank.accountservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bank.accountservice.model.events.outbox.OutboxEvent;
import com.bank.accountservice.model.events.outbox.OutboxStatus;

import java.util.List;
import java.util.UUID;


public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findByStatus(OutboxStatus pending);

    @Query("""
        SELECT e FROM OutboxEvent e
        WHERE e.status = 'PENDING'
        ORDER BY e.createdAt
    """)
    Page<OutboxEvent> findNextPending(Pageable pageable);

}