package com.bank.accountservice.config.kafka;

import java.util.Map;

import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

@Configuration(proxyBeanMethods = false)
public class KafkaAdminConfiguration {

    @Bean
    public KafkaAdmin kafkaAdmin(
            KafkaProperties kafkaProperties,
            ObjectProvider<SslBundles> sslBundlesProvider
    ) {
        SslBundles sslBundles = sslBundlesProvider.getIfAvailable();
        Map<String, Object> configs =
                kafkaProperties.buildAdminProperties(sslBundles);
        return new KafkaAdmin(configs);
    }

    @Component
    public class KafkaHealthIndicator implements HealthIndicator {

        private final AdminClient adminClient;

        public KafkaHealthIndicator(KafkaAdmin kafkaAdmin) {
            this.adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties());
        }

        @Override
        public Health health() {
            try {
                adminClient.describeCluster().clusterId().get();
                return Health.up().build();
            } catch (Exception e) {
                return Health.down(e).build();
            }
        }
    }

}

