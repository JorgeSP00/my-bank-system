package com.bank.transactionservice.exception;

public class AccountNotFound extends RuntimeException {
    public AccountNotFound(String message) { super(message); }
    public AccountNotFound(String message, Throwable cause) { super(message, cause); }
}
