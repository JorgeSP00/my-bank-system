package com.bank.accountservice.observability;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.util.StopWatch;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class TraceService {
    
    private final MetricService metricService;
    private final Map<String, Logger> loggers = new ConcurrentHashMap<>();

    public void trace(String key, StopWatch stopWatch) {
        trace(key, String.format("Trace execution {}", key), stopWatch);
    }

    private void trace(String key, String message, StopWatch stopWatch) {
        getCategoryLogger(key).info("{} ({} seconds)", message, stopWatch.getTotalTimeSeconds());
        metricService.timer(key).record(stopWatch.getTotalTimeNanos(), TimeUnit.NANOSECONDS);
    }

    private final Logger getCategoryLogger(String category) {
        return loggers.computeIfAbsent(category, cat -> LoggerFactory.getLogger(log.getName() + "." + cat));
    }
}
