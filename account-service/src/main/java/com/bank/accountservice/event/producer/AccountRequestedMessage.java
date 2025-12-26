package com.bank.accountservice.event.producer;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequestedMessage {
    private UUID accountId;
    private String accountNumber;
    private String status;
    private Long version;
}
