package com.bank.accountservice.exception;

public class CouldNotProcessTransaction extends RuntimeException {
    public CouldNotProcessTransaction(String message) {
        super(message);
    }

    public CouldNotProcessTransaction(String message, Throwable cause) {
        super(message, cause);
    }
}