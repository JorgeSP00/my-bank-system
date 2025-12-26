package com.bank.accountservice.exception;

public class NotPossibleToUpdateAccount extends RuntimeException {
    public NotPossibleToUpdateAccount(String message) {
        super(message);
    }

    public NotPossibleToUpdateAccount(String message, Throwable cause) {
        super(message, cause);
    }
}