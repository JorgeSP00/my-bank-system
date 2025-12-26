package com.bank.transactionservice.kafka.consumer;

import com.bank.transactionservice.event.consumer.AccountProcessedEvent;
import com.bank.transactionservice.kafka.KafkaTopics;
import com.bank.transactionservice.service.AccountService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Consumer de eventos de cuentas actualizadas.
 * Sincroniza los cambios de las cuentas del servicio de cuentas en este servicio.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaAccountUpdatedConsumer {
    
    private final AccountService accountService;

    /**
     * Consume eventos de cuentas actualizadas desde Kafka.
     * Sincroniza los cambios en la base de datos local.
     */
    @KafkaListener(
        topics = KafkaTopics.ACCOUNT_UPDATED, 
        groupId = "transaction-service-group",
        containerFactory = "accountProcessedEventKafkaListenerContainerFactory"
    )
    public void consumeAccountUpdated(AccountProcessedEvent event, Acknowledgment ack) {
        UUID transactionId = UUID.randomUUID();
        try {
            log.debug("[KafkaAccountUpdatedConsumer] [TxId: {}] Received AccountUpdatedEvent - AccountId: {}", 
                transactionId, event.accountId());
            
            accountService.updateAccountFromConsumer(event);
            ack.acknowledge();
            log.info("[KafkaAccountUpdatedConsumer] [TxId: {}] ✅ Account updated successfully - AccountId: {}", 
                transactionId, event.accountId());
        } catch (Exception e) {
            log.error("[KafkaAccountUpdatedConsumer] [TxId: {}] ❌ Error processing AccountUpdatedEvent - AccountId: {}, Error: {}", 
                transactionId, event.accountId(), e.getMessage(), e);
            throw e;
        }
    }
}
