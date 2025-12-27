package com.bank.transactionservice.kafka.consumer;

import com.bank.transactionservice.event.consumer.AccountProcessedEvent;
import com.bank.transactionservice.kafka.KafkaTopics;
import com.bank.transactionservice.model.model.processedevent.ProcessedEvent;
import com.bank.transactionservice.repository.ProcessedEventRepository;
import com.bank.transactionservice.service.AccountService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
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

    private final ProcessedEventRepository processedEventRepository;
    
    /**
     * Consume eventos de cuentas actualizadas desde Kafka.
     * Sincroniza los cambios en la base de datos local.
     */
    @KafkaListener(
        topics = KafkaTopics.ACCOUNT_UPDATED, 
        groupId = "transaction-service-group",
        containerFactory = "accountProcessedEventKafkaListenerContainerFactory"
    )
    public void consumeAccountUpdated(AccountProcessedEvent event, 
            Acknowledgment ack,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header("X-Event-Id") String eventIdHeader) {
        UUID eventUuid = UUID.fromString(eventIdHeader);
        try {

            if (processedEventRepository.existsById(eventUuid)) {
                log.info("Evento duplicado ignorado. eventId={}", eventUuid);
                ack.acknowledge();
                return;
            }
            log.debug("[KafkaAccountUpdatedConsumer] [TxId: {}] Received AccountUpdatedEvent - AccountId: {}", 
                eventUuid, event.accountId());
            
            accountService.updateAccountFromConsumer(event);

            processedEventRepository.save(
                new ProcessedEvent(
                    eventUuid,
                    event.getClass().getSimpleName(),
                    topic,
                    partition,
                    offset
                )
            );
            
            ack.acknowledge();
            log.info("[KafkaAccountUpdatedConsumer] [TxId: {}] ✅ Account updated successfully - AccountId: {}", 
                eventUuid, event.accountId());
        } catch (Exception e) {
            log.error("[KafkaAccountUpdatedConsumer] [TxId: {}] ❌ Error processing AccountUpdatedEvent - AccountId: {}, Error: {}", 
                eventUuid, event.accountId(), e.getMessage(), e);
            throw e;
        }
    }
}
