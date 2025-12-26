package com.bank.transactionservice.event.producer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequestedMessage {
    private UUID transactionId;
    private UUID fromAccountId;
    private Long fromAccountVersionId;
    private UUID toAccountId;
    private Long toAccountVersionId;
    private BigDecimal amount;
}
