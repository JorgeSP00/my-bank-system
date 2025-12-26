package com.bank.transactionservice.exception;

public class TransactionNotFound extends RuntimeException {
    public TransactionNotFound(String message) { super(message); }
    public TransactionNotFound(String message, Throwable cause) { super(message, cause); }
}
