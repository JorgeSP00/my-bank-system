package com.bank.transactionservice.kafka.consumer;

import com.bank.transactionservice.event.consumer.TransactionCompletedEvent;
import com.bank.transactionservice.kafka.KafkaTopics;
import com.bank.transactionservice.service.TransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

/**
 * Consumer de eventos de transacciones completadas.
 * Actualiza el estado de las transacciones cuando se completan en el servicio de cuentas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaCompletedTransactionConsumer {
    
    private final TransactionService transactionService;

    /**
     * Consume eventos de transacciones completadas desde Kafka.
     * Actualiza el estado de la transacción en la base de datos local.
     */
    @KafkaListener(
        topics = KafkaTopics.TRANSACTION_COMPLETED, 
        groupId = "transaction-service-group",
        containerFactory = "transactionCompletedEventKafkaListenerContainerFactory"
    )
    public void consume(TransactionCompletedEvent event, Acknowledgment ack) throws JsonProcessingException {
        java.util.UUID transactionId = event.transactionId();
        try {
            log.debug("[KafkaCompletedTransactionConsumer] Received TransactionCompletedEvent - TransactionId: {}", transactionId);
            
            transactionService.updateTransaction(transactionId, event.transactionStatus(), event.observations());
            ack.acknowledge();
            log.info("[KafkaCompletedTransactionConsumer] ✅ Transaction updated successfully - TransactionId: {}, NewStatus: {}", 
                transactionId, event.transactionStatus());
        } catch (Exception e) {
            log.error("[KafkaCompletedTransactionConsumer] ❌ Error processing TransactionCompletedEvent - TransactionId: {}, Error: {}", 
                transactionId, e.getMessage(), e);
            throw e;
        }
    }
}
