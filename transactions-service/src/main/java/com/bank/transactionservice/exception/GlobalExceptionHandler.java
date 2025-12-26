package com.bank.transactionservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para el servicio de transacciones.
 * Proporciona respuestas consistentes y trazables para todos los errores.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFound.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFound(AccountNotFound ex) {
        String errorId = UUID.randomUUID().toString();
        log.warn("[ErrorId: {}] Account not found - Message: {}", errorId, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), errorId));
    }

    @ExceptionHandler(TransactionNotFound.class)
    public ResponseEntity<ErrorResponse> handleTransactionNotFound(TransactionNotFound ex) {
        String errorId = UUID.randomUUID().toString();
        log.warn("[ErrorId: {}] Transaction not found - Message: {}", errorId, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), errorId));
    }

    @ExceptionHandler(InvalidTransactionData.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransactionData(InvalidTransactionData ex) {
        String errorId = UUID.randomUUID().toString();
        log.warn("[ErrorId: {}] Invalid transaction data - Message: {}", errorId, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), errorId));
    }

    @ExceptionHandler(EventSerializationException.class)
    public ResponseEntity<ErrorResponse> handleEventSerialization(EventSerializationException ex) {
        String errorId = UUID.randomUUID().toString();
        log.error("[ErrorId: {}] Event serialization error - Message: {}", errorId, ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), errorId));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String errorId = UUID.randomUUID().toString();
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("[ErrorId: {}] Validation failed - Errors: {}", errorId, errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Validation failed: " + errors, errorId));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        String errorId = UUID.randomUUID().toString();
        log.error("[ErrorId: {}] Unexpected error - Message: {}", errorId, ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    "Unexpected error occurred. Error ID: " + errorId, errorId));
    }

    public record ErrorResponse(int status, String message, String errorId, LocalDateTime timestamp) {
        public ErrorResponse(int status, String message, String errorId) {
            this(status, message, errorId, LocalDateTime.now());
        }
    }
}
