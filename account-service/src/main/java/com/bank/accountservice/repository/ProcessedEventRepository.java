package com.bank.accountservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.accountservice.model.events.processedevent.ProcessedEvent;

import java.util.UUID;


public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {
}
