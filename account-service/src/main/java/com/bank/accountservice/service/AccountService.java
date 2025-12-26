package com.bank.accountservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.accountservice.exception.AccountAlreadyExists;
import com.bank.accountservice.exception.AccountNotFound;
import com.bank.accountservice.model.account.Account;
import com.bank.accountservice.model.account.AccountStatus;
import com.bank.accountservice.repository.AccountRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio de gestión de cuentas.
 * Responsable de crear, actualizar y recuperar cuentas bancarias.
 * Implementa el patrón Outbox para eventos de cuentas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final OutboxService outboxService;

    public List<Account> findAllAccounts() {
        log.debug("[AccountService] Retrieving all accounts");
        return accountRepository.findAll();
    }

    public Account getAccountEntityById(UUID id) {
        log.debug("[AccountService] Getting account by ID - AccountId: {}", id);
        return accountRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[AccountService] Account not found - AccountId: {}", id);
                    return new AccountNotFound("Account with ID " + id + " not found");
                });
    }

    public Account getAccountByIdWithLock(UUID id) {
        log.debug("[AccountService] Getting account by ID with lock - AccountId: {}", id);
        return accountRepository.findByIdWithLock(id)
                .orElseThrow(() -> {
                    log.warn("[AccountService] Account not found - AccountId: {}", id);
                    return new AccountNotFound("Account with ID " + id + " not found");
                });
    }

    @Transactional
    public Account updateAccount(Account account) {
        UUID transactionId = UUID.randomUUID();
        log.info("[AccountService] [TxId: {}] Updating account - AccountId: {}, AccountNumber: {}", 
            transactionId, account.getId(), account.getAccountNumber());
        
        Optional<Account> existing = accountRepository.findById(account.getId());
        if(existing.isPresent()) {
            Account existingAccount = existing.get();
            existingAccount.setAccountNumber(account.getAccountNumber());
            existingAccount.setOwnerName(account.getOwnerName());
            existingAccount.setStatus(account.getStatus());
            existingAccount.setBalance(account.getBalance());
            existingAccount.setVersionId(existingAccount.getVersionId() + 1);
            saveAccount(existingAccount);
            outboxService.saveAccountUpdatedEvent(existingAccount);
            log.info("[AccountService] [TxId: {}] ✅ Account updated successfully - AccountId: {}", transactionId, account.getId());
            return existingAccount;
        } else {
            log.warn("[AccountService] [TxId: {}] Account not found for update - AccountId: {}", transactionId, account.getId());
            throw new AccountNotFound("Account with ID " + account.getId() + " not found");
        }
    }

    public Account saveAccount(Account account) {
        Account saved = accountRepository.save(account);
        return saved;
    }

    @Transactional
    public Account createAccount(Account account) {
        UUID transactionId = UUID.randomUUID();
        log.info("[AccountService] [TxId: {}] Creating new account - AccountNumber: {}, Owner: {}", 
            transactionId, account.getAccountNumber(), account.getOwnerName());
        
        Optional<Account> existing = accountRepository.findByAccountNumber(account.getAccountNumber());
        if (existing.isPresent()) {
            log.warn("[AccountService] [TxId: {}] Account already exists - AccountNumber: {}", 
                transactionId, account.getAccountNumber());
            throw new AccountAlreadyExists("Account with account number " + account.getAccountNumber() + " already exists");
        }
        else {
            Account saved = accountRepository.save(account);
            outboxService.saveAccountCreatedEvent(saved);
            log.info("[AccountService] [TxId: {}] ✅ Account created successfully - AccountId: {}, AccountNumber: {}", 
                transactionId, saved.getId(), saved.getAccountNumber());
            return saved;
        }
    }

    public boolean checkAccountAvailable(Long accountVersionId, Account account) {
        return 
            account.getVersionId().equals(accountVersionId) 
            && account.getStatus().equals(AccountStatus.ACTIVE);
    }

    public boolean checkFoundsInAccount(BigDecimal amount, Account account) {
        return account.getBalance().compareTo(amount) != -1;
    }
    
    public void addMoneyToAccount(Account account, BigDecimal amount) {
        account.setBalance(account.getBalance().add(amount));
        updateAccount(account);
    }

    public void removeMoneyFromAccount(Account account, BigDecimal amount) {
        account.setBalance(account.getBalance().subtract(amount));
        updateAccount(account);
    }
}
