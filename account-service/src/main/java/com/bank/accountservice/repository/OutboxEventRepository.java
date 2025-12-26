package com.bank.accountservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.accountservice.model.outbox.OutboxEvent;
import com.bank.accountservice.model.outbox.OutboxStatus;

import java.util.List;
import java.util.UUID;


public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findByStatus(OutboxStatus pending);
}