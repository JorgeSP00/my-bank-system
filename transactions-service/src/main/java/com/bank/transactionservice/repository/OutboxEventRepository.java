package com.bank.transactionservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.bank.transactionservice.model.outbox.OutboxEvent;
import com.bank.transactionservice.model.outbox.OutboxStatus;

import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findByStatus(OutboxStatus status);

    @Query("""
        SELECT e FROM OutboxEvent e
        WHERE e.status = 'PENDING'
        ORDER BY e.createdAt
    """)
    Page<OutboxEvent> findNextPending(Pageable pageable);
}
