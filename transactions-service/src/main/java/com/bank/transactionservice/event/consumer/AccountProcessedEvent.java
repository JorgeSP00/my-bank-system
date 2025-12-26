package com.bank.transactionservice.event.consumer;

import java.util.UUID;

import com.bank.transactionservice.model.account.AccountStatus;


public record AccountProcessedEvent(
    UUID accountId,
    String accountNumber,
    AccountStatus status,
    Long version
) {}
