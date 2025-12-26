package com.bank.transactionservice.exception;

public class InvalidTransactionData extends RuntimeException {
    public InvalidTransactionData(String message) { super(message); }
    public InvalidTransactionData(String message, Throwable cause) { super(message, cause); }
}
