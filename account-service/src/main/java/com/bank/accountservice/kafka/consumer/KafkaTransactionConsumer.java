package com.bank.accountservice.kafka.consumer;

import com.bank.accountservice.event.consumer.TransactionProcessedEvent;
import com.bank.accountservice.kafka.KafkaTopics;
import com.bank.accountservice.service.TransactionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaTransactionConsumer {
    
    private final TransactionService transactionService;

    /**
     * Consume eventos de transacciones procesadas desde Kafka.
     * Actualiza el estado de la transacción en la base de datos.
     */
    @KafkaListener(topics = KafkaTopics.TRANSACTION_REQUESTED, groupId = "account-service-group", 
                   containerFactory = "transactionProcessedEventKafkaListenerContainerFactory")
    public void consume(TransactionProcessedEvent event, Acknowledgment ack) {
        UUID transactionId = UUID.randomUUID();
        try {
            log.debug("[KafkaTransactionConsumer] [TxId: {}] Received message - EventType: {}, Payload: {}", 
                transactionId, event.getClass().getSimpleName(), event);
            
            transactionService.doTransaction(event);
            ack.acknowledge();
            log.info("[KafkaTransactionConsumer] [TxId: {}] ✅ Event processed successfully - EventType: {}", 
                transactionId, event.getClass().getSimpleName());
                
        } catch (Exception e) {
            log.error("[KafkaTransactionConsumer] [TxId: {}] ❌ Error processing event - EventType: {}, Error: {}", 
                transactionId, event.getClass().getSimpleName(), e.getMessage(), e);
            throw e;
        }
    }
}
