package com.bank.accountservice.config.persistence;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        // Más adelante se puede obtener del contexto de seguridad
        return Optional.of("system");
    }
}