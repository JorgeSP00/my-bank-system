package com.bank.accountservice.kafka.consumer;

import com.bank.accountservice.event.consumer.TransactionProcessedEvent;
import com.bank.accountservice.kafka.KafkaTopics;
import com.bank.accountservice.model.events.processedevent.ProcessedEvent;
import com.bank.accountservice.repository.ProcessedEventRepository;
import com.bank.accountservice.service.TransactionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaTransactionConsumer {
    
    private final TransactionService transactionService;

    private final ProcessedEventRepository processedEventRepository;
    
    /**
     * Consume eventos de transacciones procesadas desde Kafka.
     * Actualiza el estado de la transacción en la base de datos.
     */
    @KafkaListener(topics = KafkaTopics.TRANSACTION_REQUESTED, groupId = "account-service-group", 
                   containerFactory = "transactionProcessedEventKafkaListenerContainerFactory")
    public void consume(TransactionProcessedEvent event, 
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

            log.debug("[KafkaTransactionConsumer] [TxId: {}] Received message - EventType: {}, Payload: {}", 
                eventUuid, event.getClass().getSimpleName(), event);
            
            transactionService.doTransaction(event);
            
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
            log.info("[KafkaTransactionConsumer] [TxId: {}] ✅ Event processed successfully - EventType: {}", 
                eventUuid, event.getClass().getSimpleName());
                
        } catch (Exception e) {
            log.error("[KafkaTransactionConsumer] [TxId: {}] ❌ Error processing event - EventType: {}, Error: {}", 
                eventUuid, event.getClass().getSimpleName(), e.getMessage(), e);
            throw e;
        }
    }
}
