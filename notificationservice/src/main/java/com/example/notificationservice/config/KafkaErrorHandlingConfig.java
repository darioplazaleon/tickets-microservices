package com.example.notificationservice.config;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

/**
 * Reintentos con backoff exponencial y dead-letter topic ({@code <topic>.DLT})
 * para todos los @KafkaListener del servicio. Spring Boot detecta este bean
 * (CommonErrorHandler) y lo aplica al container factory por defecto.
 */
@Configuration
public class KafkaErrorHandlingConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(ProducerFactory<String, Object> producerFactory) {
        // Un fallo de deserialización llega como byte[] crudo y va al DLT con un
        // producer de bytes; los eventos ya deserializados van con el JsonSerializer.
        Map<String, Object> bytesProps = new HashMap<>(producerFactory.getConfigurationProperties());
        bytesProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);

        Map<Class<?>, KafkaOperations<?, ?>> templates = new LinkedHashMap<>();
        templates.put(byte[].class, new KafkaTemplate<>(new DefaultKafkaProducerFactory<String, byte[]>(bytesProps)));
        templates.put(Object.class, new KafkaTemplate<>(producerFactory));

        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
        backOff.setInitialInterval(1_000);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(10_000);

        DefaultErrorHandler handler =
                new DefaultErrorHandler(new DeadLetterPublishingRecoverer(templates), backOff);
        // Errores de validación de negocio: reintentar no los arregla.
        handler.addNotRetryableExceptions(IllegalArgumentException.class);
        return handler;
    }
}
