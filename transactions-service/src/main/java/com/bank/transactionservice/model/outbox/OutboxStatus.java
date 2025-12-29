package com.bank.transactionservice.model.outbox;

public enum OutboxStatus {
    PENDING,
    SENT,
    FAILED
}
