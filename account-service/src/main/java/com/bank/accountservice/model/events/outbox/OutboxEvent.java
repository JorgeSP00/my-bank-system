package com.bank.accountservice.model.events.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_event", indexes = {
    @Index(name = "idx_outbox_status_created", columnList = "status, created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {

    @Id
    private UUID id;

    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType; // ej: "Account"

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId; // id de la entidad principal

    @Column(name = "type", nullable = false)
    private String type; // ej: "AccountRequestedMessage"

    @Column(name = "topic", nullable = false)
    private String topic; // ej: "account.requested"

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload; // JSON serializado del evento

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @PrePersist
    public void prePersist() {
        this.id = UUID.randomUUID();
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = OutboxStatus.PENDING;
        }
    }

}
