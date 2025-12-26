package com.bank.transactionservice.exception;

public class EventSerializationException extends RuntimeException {
    public EventSerializationException(String message) { super(message); }
    public EventSerializationException(String message, Throwable cause) { super(message, cause); }
}
