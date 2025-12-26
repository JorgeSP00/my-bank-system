package com.bank.accountservice.event.consumer;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionProcessedEvent(
    UUID transactionId,
    UUID fromAccountId,
    Long fromAccountVersionId,
    UUID toAccountId,
    Long toAccountVersionId,
    BigDecimal amount
) {}
