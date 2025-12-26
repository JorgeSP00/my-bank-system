package com.bank.accountservice.service;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.bank.accountservice.event.consumer.TransactionProcessedEvent;
import com.bank.accountservice.exception.CouldNotSerializeEvent;
import com.bank.accountservice.kafka.KafkaTopics;
import com.bank.accountservice.mapper.AccountMapper;
import com.bank.accountservice.model.account.Account;
import com.bank.accountservice.model.outbox.OutboxEvent;
import com.bank.accountservice.model.transaction.TransactionStatus;
import com.bank.accountservice.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final AccountMapper accountMapper;

    /**
     * Guarda un evento en la tabla outbox_event para posterior publicación a Kafka.
     *
     * @param aggregateType tipo de la entidad (ej: "Transaction", "Account")
     * @param aggregateId   ID de la entidad
     * @param eventType     tipo del evento (ej: "TransactionRequestedMessage")
     * @param topic         tema de Kafka donde se publicará el evento
     * @param payload       objeto del evento a serializar
     * @throws EventSerializationException si hay error en la serialización
     */
    public void saveOutboxEvent(String aggregateType, UUID aggregateId, String eventType, String topic, Object payload) {
        UUID transactionId = UUID.randomUUID();
        try {
            log.debug("[OutboxEventService] [TxId: {}] Serializing payload for event - AggregateType: {}, AggregateId: {}, EventType: {}", 
                transactionId, aggregateType, aggregateId, eventType);
            
            // Serializar el payload a JSON string
            String payloadJson = objectMapper.writeValueAsString(payload);

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .type(eventType)
                    .topic(topic)
                    .payload(payloadJson)
                    .build();

            outboxEventRepository.save(outboxEvent);
            
            log.info("[OutboxEventService] [TxId: {}] ✅ Event saved to outbox - EventType: {}, Topic: {}, AggregateId: {}, EventId: {}", 
                transactionId, eventType, topic, aggregateId, outboxEvent.getId());
                
        } catch (Exception e) {
            log.error("[OutboxEventService] [TxId: {}] ❌ Failed to save outbox event - AggregateType: {}, AggregateId: {}, EventType: {}, Error: {}", 
                transactionId, aggregateType, aggregateId, eventType, e.getMessage(), e);
            throw new CouldNotSerializeEvent("Failed to save outbox event for " + eventType + " with aggregateId " + aggregateId, e);
        }
    }

    public void completeTransaction(TransactionProcessedEvent transactionProcessedEvent, TransactionStatus transactionState) {
        try {
            log.debug("[AccountService] [TxId: {}] Saving TransactionProcessedEvent - TransactionId: {}", transactionProcessedEvent.transactionId(), transactionProcessedEvent.transactionId());
            Map<String, Object> payload = Map.of(
                "transactionId", transactionProcessedEvent.transactionId().toString(),
                "transactionStatus", transactionState.name(),
                "observations", "null"
            );
            saveOutboxEvent(
                "Transaction", 
                transactionProcessedEvent.transactionId(), 
                "TransactionProcessedEvent", 
                KafkaTopics.TRANSACTION_COMPLETED, 
                payload
            );
        } catch (CouldNotSerializeEvent e) {
            throw new CouldNotSerializeEvent("Failed to serialize TransactionProcessedEvent", e);
        }
    }

    public void saveAccountCreatedEvent(Account account) {
        try {
            log.debug("[AccountService] [TxId: {}] Saving AccountCreatedEvent - AccountId: {}", account.getId(), account.getId());
            saveOutboxEvent(
                "Account",
                account.getId(),
                "AccountCreatedEvent",
                KafkaTopics.ACCOUNT_CREATED,
                accountMapper.fromEntityToMessage(account)
            );
        } catch (CouldNotSerializeEvent e) {
            log.error("[AccountService] Failed to serialize AccountCreatedEvent - AccountId: {}, Error: {}", 
                account.getId(), e.getMessage(), e);
            throw new CouldNotSerializeEvent("Failed to serialize AccountCreatedEvent", e);
        }
    }

    public void saveAccountUpdatedEvent(Account account) {
        try {
            log.debug("[AccountService] [TxId: {}] Saving AccountUpdatedEvent - AccountId: {}", account.getId(), account.getId());
            saveOutboxEvent(
                "Account", 
                account.getId(), 
                "AccountUpdatedEvent", 
                KafkaTopics.ACCOUNT_UPDATED, 
                accountMapper.fromEntityToMessage(account)
            );
            log.debug("[AccountService] [TxId: {}] AccountUpdatedEvent saved to outbox - EventId: {}", account.getId(), account.getId());
            
        } catch (CouldNotSerializeEvent e) {
            log.error("[AccountService] Failed to serialize AccountUpdatedEvent - AccountId: {}, Error: {}", 
                account.getId(), e.getMessage(), e);
            throw new CouldNotSerializeEvent("Failed to serialize AccountUpdatedEvent", e);
        }
    }
}
