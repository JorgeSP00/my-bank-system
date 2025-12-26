package com.bank.accountservice.exception;

public class AccountAlreadyExists extends RuntimeException {
    public AccountAlreadyExists(String message) {
        super(message);
    }

    public AccountAlreadyExists(String message, Throwable cause) {
        super(message, cause);
    }
}