package com.bank.accountservice.observability;

/**
 * Constantes de métricas para observabilidad del sistema.
 * Define los nombres de las métricas utilizadas en toda la aplicación.
 */
public class Metrics {
    
    // ========== ACCOUNT SERVICE ==========
    public static final String ACCOUNT_CREATED_TOTAL = "account.created.total";
    public static final String ACCOUNT_UPDATED_TOTAL = "account.updated.total";
    public static final String ACCOUNT_RETRIEVED_TOTAL = "account.retrieved.total";
    public static final String ACCOUNT_LOCKED_TOTAL = "account.locked.total";
    
    // ========== TRANSACTION SERVICE ==========
    public static final String TRANSACTION_PROCESSED_TOTAL = "transaction.processed.total";
    public static final String TRANSACTION_CORRECT_TOTAL = "transaction.correct.total";
    public static final String TRANSACTION_INCORRECT_TOTAL = "transaction.incorrect.total";
    public static final String TRANSACTION_FAILED_TOTAL = "transaction.failed.total";
    public static final String TRANSACTION_DURATION = "transaction.duration";
    
    // ========== OUTBOX SERVICE ==========
    public static final String OUTBOX_EVENT_SAVED_TOTAL = "outbox.event.saved.total";
    public static final String OUTBOX_EVENT_FAILED_TOTAL = "outbox.event.failed.total";
    
    // ========== SAGA EXECUTIONS ==========
    public static final String SAGA_EXECUTIONS_TOTAL = "saga.executions.total";
    
    // ========== CONNECTION POOL ==========
    public static final String ACTIVE_CONNECTIONS = "active.connections";
    public static final String DATABASE_CONNECTIONS_ACTIVE = "db.connections.active";
    public static final String KAFKA_CONNECTIONS_ACTIVE = "kafka.connections.active";
}
