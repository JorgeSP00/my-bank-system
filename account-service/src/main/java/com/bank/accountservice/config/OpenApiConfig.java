package com.bank.accountservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;

/**
 * Configuración de OpenAPI para la documentación de la API del servicio de cuentas.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Account Service API")
                        .version("1.0")
                        .description("API para la gestión de cuentas bancarias")
                        .contact(new Contact()
                                .name("Bank System Team")
                                .email("support@bank-system.com")));
    }
}