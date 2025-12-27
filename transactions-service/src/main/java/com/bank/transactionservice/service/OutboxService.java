package com.bank.transactionservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.bank.transactionservice.model.model.outbox.OutboxEvent;
import com.bank.transactionservice.model.transaction.Transaction;
import com.bank.transactionservice.repository.OutboxEventRepository;
import com.bank.transactionservice.event.producer.TransactionRequestedMessage;
import com.bank.transactionservice.exception.EventSerializationException;
import com.bank.transactionservice.kafka.KafkaTopics;

import java.util.UUID;

/**
 * Servicio para gestionar eventos del patrón Outbox.
 * Responsable de serializar y guardar eventos en la tabla outbox_event.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

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
            throw new EventSerializationException("Failed to save outbox event for " + eventType + " with aggregateId " + aggregateId, e);
        }
    }

    public void saveTransaction(Transaction transaction) {
        TransactionRequestedMessage message = TransactionRequestedMessage.builder()
            .transactionId(transaction.getId())
            .fromAccountId(transaction.getFromAccount().getId())
            .fromAccountVersionId(transaction.getFromAccount().getVersionId())
            .toAccountId(transaction.getToAccount().getId())
            .toAccountVersionId(transaction.getToAccount().getVersionId())
            .amount(transaction.getAmount())
            .build();
        saveOutboxEvent(
            "Transaction",
            transaction.getId(),
            "TransactionRequestedMessage",
            KafkaTopics.TRANSACTION_REQUESTED,
            message
        );

    }
}
