package com.bank.transactionservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.bank.transactionservice.exception.InvalidTransactionData;
import com.bank.transactionservice.exception.TransactionNotFound;
import com.bank.transactionservice.model.account.Account;
import com.bank.transactionservice.model.account.AccountStatus;
import com.bank.transactionservice.model.transaction.Transaction;
import com.bank.transactionservice.model.transaction.TransactionStatus;
import com.bank.transactionservice.model.transaction.TransactionType;
import com.bank.transactionservice.repository.TransactionRepository;

/**
 * Servicio de gestión de transacciones.
 * Responsable de crear, validar y actualizar transacciones bancarias.
 * Implementa el patrón Outbox para eventos de transacciones.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {
    private final AccountService accountService;
    private final TransactionRepository transactionRepository;
    private final OutboxService outboxService;

    public List<Transaction> getAllTransactions() {
        log.debug("[TransactionService] Retrieving all transactions");
        return transactionRepository.findAll();
    }

    public Transaction getTransactionById(UUID id) {
        log.debug("[TransactionService] Retrieving transaction - TransactionId: {}", id);
        Transaction t = transactionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[TransactionService] Transaction not found - TransactionId: {}", id);
                    return new TransactionNotFound("Transaction with ID " + id + " not found");
                });
        return t;
    }

    @Transactional
    public Transaction createTransaction(String fromAccountNumber, String toAccountNumber, BigDecimal amount, TransactionType type, String description) {
        UUID transactionId = UUID.randomUUID();
        log.info("[TransactionService] [TxId: {}] Creating new transaction - FromAccount: {}, ToAccount: {}, Amount: {}", 
            transactionId, fromAccountNumber, toAccountNumber, amount);

        if(fromAccountNumber.equals(toAccountNumber)) {
            log.warn("[TransactionService] [TxId: {}] Invalid transaction data: FromAccount and ToAccount are the same - AccountNumber: {}", 
                transactionId, fromAccountNumber);
            throw new InvalidTransactionData("FromAccount and ToAccount cannot be the same");
        }
        
        Account fromAccount = accountService.getByAccountNumber(fromAccountNumber);
        Account toAccount = accountService.getByAccountNumber(toAccountNumber);

        validateTransaction(fromAccount, toAccount, amount, type, description);
        
        
        Transaction t = Transaction.builder()
                .amount(amount)
                .description(description)
                .type(type)
                .build();
        t.setId(UUID.randomUUID());
        t.setFromAccount(fromAccount);
        t.setToAccount(toAccount);
        t.setFromAccountVersionId(fromAccount.getVersionId());
        t.setToAccountVersionId(toAccount.getVersionId());
        t.setStatus(TransactionStatus.PENDING);
        t.setObservations("Started Transaction");
        Transaction saved = transactionRepository.save(t);
        outboxService.saveTransaction(saved);
        
        log.info("[TransactionService] [TxId: {}] ✅ Transaction created successfully - TransactionId: {}, Status: {}", 
            transactionId, saved.getId(), saved.getStatus());
        return saved;
    }

    private void validateTransaction(Account fromAccount, Account toAccount, BigDecimal amount, TransactionType type, String description) {
        UUID transactionId = UUID.randomUUID();
        log.debug("[TransactionService] [TxId: {}] Validating transaction - FromAccount: {}, ToAccount: {}", 
            transactionId, fromAccount.getAccountNumber(), toAccount.getAccountNumber());
        
        if (!fromAccount.getStatus().equals(AccountStatus.ACTIVE) || !toAccount.getStatus().equals(AccountStatus.ACTIVE)) {
            log.warn("[TransactionService] [TxId: {}] Invalid account status - FromAccountStatus: {}, ToAccountStatus: {}", 
                transactionId, fromAccount.getStatus(), toAccount.getStatus());
            throw new InvalidTransactionData("One or both accounts are not ACTIVE");
        }
        
        log.debug("[TransactionService] [TxId: {}] Transaction validated - TransactionId: {}", transactionId, transactionId);
    }

    public void updateTransaction(UUID transactionId, TransactionStatus newStatus, String observations) {
        log.info("[TransactionService] Updating transaction - TransactionId: {}, NewStatus: {}", 
            transactionId, newStatus);
        
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> {
                    log.warn("[TransactionService] Transaction not found for update - TransactionId: {}", transactionId);
                    return new TransactionNotFound("Transaction with ID " + transactionId + " not found");
                });
        
        transaction.setStatus(newStatus);
        transaction.setObservations(observations);
        transactionRepository.save(transaction);
        
        log.info("[TransactionService] ✅ Transaction updated successfully - TransactionId: {}, NewStatus: {}", 
            transactionId, transaction.getStatus());
    }
}