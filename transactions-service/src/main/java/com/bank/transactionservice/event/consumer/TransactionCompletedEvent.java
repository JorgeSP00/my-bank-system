package com.bank.transactionservice.event.consumer;

import java.util.UUID;

import com.bank.transactionservice.model.transaction.TransactionStatus;

public record TransactionCompletedEvent(
    UUID transactionId,
    TransactionStatus transactionStatus,
    String observations
) {}
