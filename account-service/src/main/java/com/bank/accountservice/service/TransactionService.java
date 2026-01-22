package com.bank.accountservice.service;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.accountservice.event.consumer.TransactionProcessedEvent;
import com.bank.accountservice.exception.AccountNotFound;
import com.bank.accountservice.exception.CouldNotProcessTransaction;
import com.bank.accountservice.model.account.Account;
import com.bank.accountservice.model.transaction.TransactionStatus;
import com.bank.accountservice.observability.MetricService;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final AccountService accountService;
    private final OutboxService outboxService;
    private final MetricService metricService;

    @Transactional
    public void doTransaction(TransactionProcessedEvent transactionProcessedEvent) {
        metricService.transactionTimer("transaction_processing").record(() -> {
            TransactionStatus transactionState;
            try {
                if (transactionProcessedEvent.toAccountId().equals(transactionProcessedEvent.fromAccountId())) {
                    log.warn("TransactionService - Invalid transaction data: FromAccount and ToAccount are the same - AccountId: {}",
                        transactionProcessedEvent.fromAccountId());
                    transactionState = TransactionStatus.INCORRECT;
                    metricService.transactionProcessed("incorrect").increment();
                    outboxService.completeTransaction(transactionProcessedEvent, transactionState);
                    return;
                }
                Account fromAccount = accountService.getAccountByIdWithLock(transactionProcessedEvent.fromAccountId());
                Account toAccount = accountService.getAccountByIdWithLock(transactionProcessedEvent.toAccountId());
                //TODO: Create more states, not doing it now to simplify the flow
                if (!accountService.checkAccountAvailable(transactionProcessedEvent.fromAccountVersionId(), fromAccount)) {
                    transactionState = TransactionStatus.INCORRECT;
                    log.warn("TransactionService - Transaction incorrect due to fromAccount issues");
                    metricService.transactionProcessed("incorrect").increment();
                } else if (!accountService.checkFoundsInAccount(transactionProcessedEvent.amount(), fromAccount)) {
                    transactionState = TransactionStatus.INCORRECT;
                    log.warn("TransactionService - Transaction incorrect due to insufficient funds");
                    metricService.transactionProcessed("incorrect").increment();
                } else if(!accountService.checkAccountAvailable(transactionProcessedEvent.toAccountVersionId(), toAccount)) {
                    transactionState = TransactionStatus.INCORRECT;
                    log.warn("TransactionService - Transaction incorrect due to toAccount issues");
                    metricService.transactionProcessed("incorrect").increment();
                } else {
                    accountService.removeMoneyFromAccount(fromAccount, transactionProcessedEvent.amount());
                    accountService.addMoneyToAccount(toAccount, transactionProcessedEvent.amount());
                    transactionState = TransactionStatus.CORRECT;
                    metricService.transactionProcessed("correct").increment();
                }
                outboxService.completeTransaction(transactionProcessedEvent, transactionState);
            } catch (AccountNotFound e) {
                log.error("TransactionService - AccountNotFoundException - TransactionId: {}",
                    transactionProcessedEvent.transactionId(), e);
                metricService.transactionProcessed("failed").increment();
                outboxService.completeTransaction(transactionProcessedEvent, TransactionStatus.FAILED);
            } catch (OptimisticLockException e) {
                log.error("TransactionService - OptimisticLockException - TransactionId: {}",
                    transactionProcessedEvent.transactionId(), e);
                metricService.transactionProcessed("failed").increment();
                throw new CouldNotProcessTransaction("Unexpected error processing transaction", e);
            } catch (DataAccessException e) {
                log.error("TransactionService - DataAccessException - TransactionId: {}",
                    transactionProcessedEvent.transactionId(), e);
                metricService.transactionProcessed("failed").increment();
                throw new CouldNotProcessTransaction("Unexpected error processing transaction", e);
            } catch (Exception e) {
                log.error("TransactionService - Unexpected error processing transaction - TransactionId: {}",
                    transactionProcessedEvent.transactionId(), e);
                metricService.transactionProcessed("failed").increment();
                throw new CouldNotProcessTransaction("Unexpected error processing transaction", e);
            }
        });
    }
}