package com.bank.accountservice.config.observability;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bank.accountservice.observability.MetricService;
import com.bank.accountservice.observability.TraceService;

import io.micrometer.core.aop.CountedAspect;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.observation.ObservationRegistry;

/**
 * Configuración de observabilidad para la aplicación.
 * Define los beans para métricas, traces y aspectos AOP.
 */
@Configuration
public class ObservabilityConfig {
    
    /**
     * Bean de TraceService para rastreo distribuido.
     */
    @Bean
    public TraceService traceService(MetricService metricService) {
        return new TraceService(metricService);
    }

    /**
     * Bean de MetricService para gestión de métricas.
     */
    @Bean
    public MetricService metricService(MeterRegistry registry) {
        return new MetricService(registry);
    }

    /**
     * Aspecto AOP para anotar métodos con @Timed.
     */
    @Bean
    @ConditionalOnMissingBean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * Aspecto AOP para anotar métodos con @Counted.
     */
    @Bean
    @ConditionalOnMissingBean
    public CountedAspect countedAspect(MeterRegistry registry) {
        return new CountedAspect(registry);
    }

    /**
     * Registro de observaciones para rastreo distribuido.
     */
    @Bean
    @ConditionalOnMissingBean
    public ObservationRegistry observationRegistry() {
        return ObservationRegistry.create();
    }

    /**
     * Registro de métricas principal.
     */
    @Bean
    @ConditionalOnMissingBean
    public MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }
}
