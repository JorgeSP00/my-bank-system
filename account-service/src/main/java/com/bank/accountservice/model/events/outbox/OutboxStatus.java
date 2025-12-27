package com.bank.accountservice.model.events.outbox;

/**
 * Estados de un evento en el patrón Outbox.
 * 
 * PENDING: Evento recién creado, pendiente de ser publicado a Kafka
 * SENT: Evento publicado exitosamente en Kafka
 * FAILED: Error al publicar el evento (puede reintentarse)
 */
public enum OutboxStatus {
    PENDING,
    SENT,
    FAILED
}