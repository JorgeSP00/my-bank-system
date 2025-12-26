package com.bank.transactionservice.mapper;


import org.springframework.stereotype.Component;

import com.bank.transactionservice.event.producer.TransactionRequestedMessage;
import com.bank.transactionservice.dto.request.TransactionRequestDTO;
import com.bank.transactionservice.dto.response.TransactionResponseDTO;
import com.bank.transactionservice.model.transaction.Transaction;

@Component
public class TransactionMapper {

    public Transaction fromRequestToEntity(TransactionRequestDTO dto) {
        return Transaction.builder()
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .type(dto.getType())
                .build();
    }

    public TransactionResponseDTO fromEntityToResponse(Transaction t) {
        return TransactionResponseDTO.builder()
                .transactionId(t.getId())
                .fromAccountNumber(t.getFromAccount().getAccountNumber())
                .toAccountNumber(t.getToAccount().getAccountNumber())
                .amount(t.getAmount())
                .type(t.getType())
                .status(t.getStatus())
                .description(t.getDescription())
                .createdAt(t.getCreatedAt())
                .build();
    }

    public TransactionRequestedMessage mapTransactionToMessage(Transaction transaction) {
        return new TransactionRequestedMessage(
            transaction.getId(),
            transaction.getFromAccount().getId(),
            transaction.getFromAccountVersionId(),
            transaction.getToAccount().getId(),
            transaction.getToAccountVersionId(),
            transaction.getAmount()
        );
    }
}

