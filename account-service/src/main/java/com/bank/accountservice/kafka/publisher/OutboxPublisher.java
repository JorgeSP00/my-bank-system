package com.bank.accountservice.kafka.publisher;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.bank.accountservice.model.events.outbox.OutboxEvent;
import com.bank.accountservice.model.events.outbox.OutboxStatus;
import com.bank.accountservice.repository.OutboxEventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Publicador de eventos Outbox.
 * Implementa el patrón Outbox para garantizar la entrega de eventos a Kafka
 * en conjunto con las transacciones de la base de datos.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.outbox.batch-size}")
    private int batchSize;

    /**
     * Publica eventos pendientes en Kafka.
     * Ejecuta cada 5 segundos para procesar eventos que aún no han sido publicados.
     */
    @Scheduled(fixedDelayString = "${spring.outbox.scheduler.delay-ms}")
    public void publishPendingEvents() {
        Page<OutboxEvent> page = outboxEventRepository
                .findNextPending(PageRequest.of(0, batchSize));

        if (page.isEmpty()) {
            log.debug("[OutboxPublisher] No pending events");
            return;
        }

        log.info("[OutboxPublisher] Processing {} events", page.getNumberOfElements());

        page.forEach(this::publishEventAsync);
    }

    private void publishEventAsync(OutboxEvent event) {
        UUID txId = UUID.randomUUID();

        Message<String> message = MessageBuilder
                .withPayload(event.getPayload())
                .setHeader(KafkaHeaders.TOPIC, event.getTopic())
                .setHeader("X-Event-Id", event.getId().toString())
                .setHeader("X-Aggregate-Id", event.getAggregateId().toString())
                .setHeader("X-Event-Type", event.getType())
                .setHeader("X-Aggregate-Type", event.getAggregateType())
                .setHeader("X-Timestamp", event.getCreatedAt().toString())
                .build();

        kafkaTemplate.send(message)
            .thenAccept(result -> onSuccess(event, txId, event.getTopic()))
            .exceptionally(ex -> {
                onFailure(event, txId, event.getTopic(), ex);
                return null;
            });

    }

    @Transactional
    protected void onSuccess(OutboxEvent event, UUID txId, String topic) {
        event.setStatus(OutboxStatus.SENT);
        event.setSentAt(LocalDateTime.now());
        outboxEventRepository.save(event);

        log.info(
            "[OutboxPublisher][TxId:{}] Event SENT - topic={}, type={}, aggregateId={}",
            txId, topic, event.getType(), event.getAggregateId()
        );
    }

    @Transactional
    protected void onFailure(OutboxEvent event, UUID txId, String topic, Throwable ex) {
        int attempts = event.incrementAttempts();
        if (attempts >= 5) {
            event.setStatus(OutboxStatus.FAILED);
            log.error(
                "[OutboxPublisher][TxId:{}] Event FAILED permanently after {} attempts - {}",
                txId, attempts, event.getId(), ex
            );
        } else {
            event.setStatus(OutboxStatus.PENDING);
            log.warn(
                "[OutboxPublisher][TxId:{}] Publish failed (attempt {}) - will retry",
                txId, attempts, ex
            );
        }

        outboxEventRepository.save(event);
    }



}
