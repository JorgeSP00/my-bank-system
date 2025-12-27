package com.bank.transactionservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.transactionservice.model.model.processedevent.ProcessedEvent;

import java.util.UUID;


public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {
}
