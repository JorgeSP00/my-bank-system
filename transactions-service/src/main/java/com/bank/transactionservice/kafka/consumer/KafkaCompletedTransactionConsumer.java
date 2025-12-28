package com.bank.transactionservice.kafka.consumer;

import com.bank.transactionservice.event.consumer.TransactionCompletedEvent;
import com.bank.transactionservice.kafka.KafkaTopics;
import com.bank.transactionservice.model.processedevent.ProcessedEvent;
import com.bank.transactionservice.repository.ProcessedEventRepository;
import com.bank.transactionservice.service.TransactionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
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

    private final ProcessedEventRepository processedEventRepository;
    /**
     * Consume eventos de transacciones completadas desde Kafka.
     * Actualiza el estado de la transacción en la base de datos local.
     */
    @KafkaListener(
        topics = KafkaTopics.TRANSACTION_COMPLETED, 
        groupId = "transaction-service-group",
        containerFactory = "transactionCompletedEventKafkaListenerContainerFactory"
    )
    public void consume(TransactionCompletedEvent event, 
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
            log.debug("[KafkaCompletedTransactionConsumer] Received TransactionCompletedEvent - TransactionId: {}", event.transactionId());

            transactionService.updateTransaction(event.transactionId(), event.transactionStatus(), event.observations());

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
            log.info("[KafkaCompletedTransactionConsumer] ✅ Transaction updated successfully - TransactionId: {}, NewStatus: {}", 
                event.transactionId(), event.transactionStatus());
        } catch (Exception e) {
            log.error("[KafkaCompletedTransactionConsumer] ❌ Error processing TransactionCompletedEvent - TransactionId: {}, Error: {}", 
                eventUuid, e.getMessage(), e);
            throw e;
        }
    }
}
