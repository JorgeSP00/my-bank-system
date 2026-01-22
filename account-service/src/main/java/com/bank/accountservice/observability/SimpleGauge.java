package com.bank.accountservice.observability;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import lombok.Getter;

/**
 * Gauge personalizado que mantiene un valor numérico que puede subir o bajar.
 * Envuelve un AtomicReference para proporcionar operaciones thread-safe.
 */
@Getter
public class SimpleGauge {
    
    private final AtomicReference<Double> value;
    private final String name;
    private final String description;
    private final String baseUnit;
    private final Tags tags;
    private final MeterRegistry registry;

    private SimpleGauge(Builder builder) {
        this.value = new AtomicReference<>(0.0);
        this.name = builder.name;
        this.description = builder.description;
        this.baseUnit = builder.baseUnit;
        this.tags = builder.tags;
        this.registry = builder.registry;
    }

    /**
     * Incrementa el valor del gauge en 1.
     */
    public void increment() {
        increment(1.0);
    }

    /**
     * Incrementa el valor del gauge en la cantidad especificada.
     * 
     * @param amount cantidad a incrementar
     */
    public void increment(double amount) {
        value.accumulateAndGet(amount, Double::sum);
    }

    /**
     * Decrementa el valor del gauge en 1.
     */
    public void decrement() {
        decrement(1.0);
    }

    /**
     * Decrementa el valor del gauge en la cantidad especificada.
     * 
     * @param amount cantidad a decrementar
     */
    public void decrement(double amount) {
        value.accumulateAndGet(-amount, Double::sum);
    }

    /**
     * Establece el valor del gauge a un valor específico.
     * 
     * @param newValue nuevo valor
     */
    public void setValue(double newValue) {
        value.set(newValue);
    }

    /**
     * Obtiene el valor actual del gauge.
     * 
     * @return valor actual
     */
    public double getValue() {
        return value.get();
    }

    /**
     * Registra el gauge en el MeterRegistry si está disponible.
     */
    public void register() {
        if (registry != null) {
            Gauge.builder(name, value, AtomicReference::get)
                .description(description)
                .baseUnit(baseUnit)
                .tags(tags)
                .register(registry);
        }
    }

    /**
     * Builder para crear instancias de SimpleGauge.
     */
    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static class Builder {
        private final String name;
        private String description = "";
        private String baseUnit = "";
        private Tags tags = Tags.empty();
        private MeterRegistry registry;

        public Builder(String name) {
            this.name = name;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder baseUnit(String baseUnit) {
            this.baseUnit = baseUnit;
            return this;
        }

        public Builder tag(String key, String value) {
            this.tags = this.tags.and(key, value);
            return this;
        }

        public Builder tags(Iterable<Tag> tags) {
            this.tags = Tags.of(tags);
            return this;
        }

        public Builder registry(MeterRegistry registry) {
            this.registry = registry;
            return this;
        }

        public SimpleGauge build() {
            SimpleGauge gauge = new SimpleGauge(this);
            gauge.register();
            return gauge;
        }

        public SimpleGauge register(MeterRegistry meterRegistry) {
            this.registry = meterRegistry;
            return build();
        }
    }
}
