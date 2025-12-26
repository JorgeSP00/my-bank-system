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
 * Consumer de eventos de cuentas creadas.
 * Sincroniza las cuentas del servicio de cuentas en este servicio.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaAccountCreatedConsumer {
    
    private final AccountService accountService;

    /**
     * Consume eventos de cuentas creadas desde Kafka.
     * Sincroniza la nueva cuenta en la base de datos local.
     */
    @KafkaListener(
        topics = KafkaTopics.ACCOUNT_CREATED, 
        groupId = "transaction-service-group",
        containerFactory = "accountProcessedEventKafkaListenerContainerFactory"
    )
    public void consumeAccountCreated(AccountProcessedEvent event, Acknowledgment ack) {
        UUID transactionId = UUID.randomUUID();
        try {
            log.debug("[KafkaAccountCreatedConsumer] [TxId: {}] Received AccountCreatedEvent - AccountId: {}", 
                transactionId, event.accountId());
            
            accountService.createAccountFromConsumer(event);
            ack.acknowledge();
            log.info("[KafkaAccountCreatedConsumer] [TxId: {}] ✅ Account created successfully - AccountId: {}", 
                transactionId, event.accountId());
        } catch (Exception e) {
            log.error("[KafkaAccountCreatedConsumer] [TxId: {}] ❌ Error processing AccountCreatedEvent - AccountId: {}, Error: {}", 
                transactionId, event.accountId(), e.getMessage(), e);
            throw e;
        }
    }
}
