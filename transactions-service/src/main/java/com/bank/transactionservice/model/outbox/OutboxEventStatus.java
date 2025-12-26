package com.bank.transactionservice.model.outbox;

public enum OutboxEventStatus {
    PENDING,
    SENT,
    FAILED
}
