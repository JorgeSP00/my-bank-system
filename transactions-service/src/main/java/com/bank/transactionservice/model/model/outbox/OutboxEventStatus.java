package com.bank.transactionservice.model.model.outbox;

public enum OutboxEventStatus {
    PENDING,
    SENT,
    FAILED
}
