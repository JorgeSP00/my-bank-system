package com.bank.accountservice.exception;

public class InvalidAccountData extends RuntimeException {
    public InvalidAccountData(String message) {
        super(message);
    }

    public InvalidAccountData(String message, Throwable cause) {
        super(message, cause);
    }
}