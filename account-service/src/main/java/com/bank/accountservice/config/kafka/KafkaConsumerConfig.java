package com.bank.accountservice.config.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import com.bank.accountservice.event.consumer.TransactionProcessedEvent;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Consumer factory for TransactionProcessedEvent
     * Deserializes records of topic "transaction.requested"
     */
    @Bean
    public ConsumerFactory<String, TransactionProcessedEvent> transactionProcessedEventConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, TransactionProcessedEvent.class.getName());
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.bank.transactionservice.event.producer");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TransactionProcessedEvent> transactionProcessedEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TransactionProcessedEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(transactionProcessedEventConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setCommonErrorHandler(defaultErrorHandler(kafkaTemplate));
        return factory;
    }

    @Bean
    public DefaultErrorHandler defaultErrorHandler(KafkaTemplate<String, String> kafkaTemplate) {

        DeadLetterPublishingRecoverer recoverer =
            new DeadLetterPublishingRecoverer(kafkaTemplate);
        //3 retries with a backoff of 2 seconds
        FixedBackOff backOff = new FixedBackOff(2000L, 3);

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);

        // These exceptions will not be retried and will be sent directly to the DLT
        handler.addNotRetryableExceptions(
                IllegalArgumentException.class,
                ValidationException.class
        );
        return handler;
    }

}
