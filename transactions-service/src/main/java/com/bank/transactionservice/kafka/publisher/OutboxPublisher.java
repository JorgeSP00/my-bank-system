package com.bank.transactionservice.kafka.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.bank.transactionservice.model.outbox.OutboxEvent;
import com.bank.transactionservice.model.outbox.OutboxEventStatus;
import com.bank.transactionservice.repository.OutboxEventRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Publicador de eventos Outbox.
 * Implementa el patrón Outbox para garantizar la entrega de eventos a Kafka
 * en conjunto con las transacciones de la base de datos.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Publica eventos pendientes en Kafka.
     * Ejecuta cada 5 segundos para procesar eventos que aún no han sido publicados.
     */
    @Scheduled(fixedDelayString = "${spring.scheduler.outbox.delay-ms}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatus(OutboxEventStatus.PENDING);
        
        if (pendingEvents.isEmpty()) {
            log.debug("[OutboxPublisher] No pending events to publish");
            return;
        }
        
        log.info("[OutboxPublisher] Processing {} pending events", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            publishEvent(event);
        }
    }

    /**
     * Publica un evento individual a Kafka.
     */
    private void publishEvent(OutboxEvent event) {
        UUID transactionId = UUID.randomUUID();
        
        try {
            log.debug("[OutboxPublisher] [TxId: {}] Publishing event - AggregateType: {}, AggregateId: {}, EventType: {}, Topic: {}", 
                transactionId, event.getAggregateType(), event.getAggregateId(), event.getType(), event.getTopic());
            
            // Crear mensaje con headers para mejor trazabilidad
            Message<String> message = MessageBuilder
                .withPayload(event.getPayload())
                .setHeader("X-Event-Id", event.getId().toString())
                .setHeader("X-Aggregate-Id", event.getAggregateId().toString())
                .setHeader("X-Event-Type", event.getType())
                .setHeader("X-Aggregate-Type", event.getAggregateType())
                .setHeader("X-Timestamp", event.getCreatedAt().toString())
                .setHeader(KafkaHeaders.TOPIC, event.getTopic())
                .build();
            
            kafkaTemplate.send(message).get(); // bloquea hasta confirmar
            
            // Marcar como enviado
            event.setStatus(OutboxEventStatus.SENT);
            event.setSentAt(LocalDateTime.now());
            outboxEventRepository.save(event);
            
            log.info("[OutboxPublisher] [TxId: {}] ✅ Event published successfully - Topic: {}, EventType: {}, AggregateId: {}", 
                transactionId, event.getTopic(), event.getType(), event.getAggregateId());
                
        } catch (Exception e) {
            log.error("[OutboxPublisher] [TxId: {}] ❌ Failed to publish event - Topic: {}, EventType: {}, AggregateId: {}, Error: {}", 
                transactionId, event.getTopic(), event.getType(), event.getAggregateId(), e.getMessage(), e);
            
            // Marcar como fallido
            event.setStatus(OutboxEventStatus.FAILED);
            outboxEventRepository.save(event);
        }
    }
}
