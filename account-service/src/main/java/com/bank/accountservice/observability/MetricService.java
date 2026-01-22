package com.bank.accountservice.observability;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;

/**
 * Servicio de métricas que facilita la creación y registro de métricas
 * en la aplicación.
 * Proporciona métodos para crear contadores, timers y gauges personalizados.
 */
@RequiredArgsConstructor
public class MetricService {

    private final MeterRegistry registry;

    private static final String UNIT_EXECUTIONS = "executions";
    private static final String UNIT_CONNECTIONS = "connections";

    private final Map<String, Counter> totalExecutions = new ConcurrentHashMap<>();
    private final Map<String, SimpleGauge> gauges = new ConcurrentHashMap<>();
    private final Map<String, Timer> timers = new ConcurrentHashMap<>();

    // ========== COUNTER METHODS ==========

    /**
     * Obtiene o crea un contador para sagex execuciones con tipo específico.
     * 
     * @param type tipo de saga
     * @return contador de ejecuciones
     */
    public final Counter totalSagaExecutions(String type) {
        return totalExecutions.computeIfAbsent(type, 
            t -> Counter.builder(Metrics.SAGA_EXECUTIONS_TOTAL)
                .tag("type", t)
                .description("Total number of saga executions")
                .baseUnit(UNIT_EXECUTIONS)
                .register(registry));
    }

    /**
     * Obtiene o crea un contador para eventos de cuenta creada.
     * 
     * @param status estado de la cuenta
     * @return contador de cuentas creadas
     */
    public final Counter accountCreated(String status) {
        String key = "account_created_" + status;
        return totalExecutions.computeIfAbsent(key,
            t -> Counter.builder(Metrics.ACCOUNT_CREATED_TOTAL)
                .tag("status", status)
                .description("Total number of accounts created")
                .baseUnit(UNIT_EXECUTIONS)
                .register(registry));
    }

    /**
     * Obtiene o crea un contador para eventos de cuenta actualizada.
     * 
     * @param status estado de la cuenta
     * @return contador de cuentas actualizadas
     */
    public final Counter accountUpdated(String status) {
        String key = "account_updated_" + status;
        return totalExecutions.computeIfAbsent(key,
            t -> Counter.builder(Metrics.ACCOUNT_UPDATED_TOTAL)
                .tag("status", status)
                .description("Total number of accounts updated")
                .baseUnit(UNIT_EXECUTIONS)
                .register(registry));
    }

    /**
     * Obtiene o crea un contador para eventos de transacción procesada.
     * 
     * @param transactionStatus estado de la transacción
     * @return contador de transacciones
     */
    public final Counter transactionProcessed(String transactionStatus) {
        String key = "transaction_processed_" + transactionStatus;
        return totalExecutions.computeIfAbsent(key,
            t -> Counter.builder(Metrics.TRANSACTION_PROCESSED_TOTAL)
                .tag("status", transactionStatus)
                .description("Total number of transactions processed")
                .baseUnit(UNIT_EXECUTIONS)
                .register(registry));
    }

    /**
     * Obtiene o crea un contador para eventos de outbox guardados.
     * 
     * @param eventType tipo de evento
     * @return contador de eventos guardados
     */
    public final Counter outboxEventSaved(String eventType) {
        String key = "outbox_saved_" + eventType;
        return totalExecutions.computeIfAbsent(key,
            t -> Counter.builder(Metrics.OUTBOX_EVENT_SAVED_TOTAL)
                .tag("event_type", eventType)
                .description("Total number of outbox events saved")
                .baseUnit(UNIT_EXECUTIONS)
                .register(registry));
    }

    /**
     * Obtiene o crea un contador para fallos al guardar eventos outbox.
     * 
     * @param eventType tipo de evento
     * @return contador de eventos fallidos
     */
    public final Counter outboxEventFailed(String eventType) {
        String key = "outbox_failed_" + eventType;
        return totalExecutions.computeIfAbsent(key,
            t -> Counter.builder(Metrics.OUTBOX_EVENT_FAILED_TOTAL)
                .tag("event_type", eventType)
                .description("Total number of outbox event failures")
                .baseUnit(UNIT_EXECUTIONS)
                .register(registry));
    }

    // ========== TIMER METHODS ==========

    /**
     * Obtiene o crea un timer para medir duraciones.
     * 
     * @param name nombre del timer
     * @return timer
     */
    public final Timer timer(String name) {
        return timers.computeIfAbsent(name,
            t -> Timer.builder(name)
                .description("Timer for " + name)
                .publishPercentileHistogram(true)
                .register(registry));
    }

    /**
     * Obtiene o crea un timer para medir transacciones.
     * 
     * @param transactionType tipo de transacción
     * @return timer de transacción
     */
    public final Timer transactionTimer(String transactionType) {
        String key = "transaction_timer_" + transactionType;
        return timers.computeIfAbsent(key,
            t -> Timer.builder(Metrics.TRANSACTION_DURATION)
                .tag("type", transactionType)
                .description("Transaction processing duration")
                .publishPercentileHistogram(true)
                .register(registry));
    }

    // ========== GAUGE METHODS ==========

    /**
     * Obtiene o crea un gauge para conexiones activas de un tipo específico.
     * 
     * @param type tipo de conexión
     * @return gauge de conexiones activas
     */
    public final SimpleGauge activeConnections(String type) {
        return gauges.computeIfAbsent(type,
            t -> SimpleGauge.builder(Metrics.ACTIVE_CONNECTIONS)
                .tag("type", t)
                .baseUnit(UNIT_CONNECTIONS)
                .description("Number of active connections of type " + t)
                .register(registry));
    }

    /**
     * Obtiene o crea un gauge para conexiones activas a la base de datos.
     * 
     * @return gauge de conexiones a BD
     */
    public final SimpleGauge databaseConnectionsActive() {
        return gauges.computeIfAbsent("database",
            t -> SimpleGauge.builder(Metrics.DATABASE_CONNECTIONS_ACTIVE)
                .description("Number of active database connections")
                .baseUnit(UNIT_CONNECTIONS)
                .register(registry));
    }

    /**
     * Obtiene o crea un gauge para conexiones activas a Kafka.
     * 
     * @return gauge de conexiones a Kafka
     */
    public final SimpleGauge kafkaConnectionsActive() {
        return gauges.computeIfAbsent("kafka",
            t -> SimpleGauge.builder(Metrics.KAFKA_CONNECTIONS_ACTIVE)
                .description("Number of active Kafka connections")
                .baseUnit(UNIT_CONNECTIONS)
                .register(registry));
    }

    // ========== UTILITY METHODS ==========

    /**
     * Obtiene el MeterRegistry utilizado por este servicio.
     * 
     * @return MeterRegistry
     */
    public MeterRegistry getRegistry() {
        return registry;
    }
}
