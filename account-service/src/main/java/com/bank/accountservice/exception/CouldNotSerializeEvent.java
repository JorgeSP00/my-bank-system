package com.bank.accountservice.exception;

public class CouldNotSerializeEvent extends RuntimeException {
    public CouldNotSerializeEvent(String message) {
        super(message);
    }

    public CouldNotSerializeEvent(String message, Throwable cause) {
        super(message, cause);
    }
}