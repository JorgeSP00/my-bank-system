package com.bank.transactionservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.transactionservice.model.transaction.Transaction;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    
    List<Transaction> findByFromAccountId(UUID accountId);

    List<Transaction> findByToAccountId(UUID accountId);

    default List<Transaction> findByAccountId(UUID accountId) {
        List<Transaction> list = findByFromAccountId(accountId);
        list.addAll(findByToAccountId(accountId));
        return list;
    }
}