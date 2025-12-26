package com.bank.transactionservice.dto.request;

import java.math.BigDecimal;

import com.bank.transactionservice.model.transaction.TransactionType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequestDTO {
    @NotBlank(message = "fromAccountNumber is required")
    private String fromAccountNumber;

    @NotBlank(message = "toAccountNumber is required")
    private String toAccountNumber;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "type is required")
    private TransactionType type;

    private String description;
}