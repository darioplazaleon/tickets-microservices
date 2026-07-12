package com.example.shared.infra.autoconfigure;

import com.example.shared.infra.outbox.OutboxEventWriter;
import com.example.shared.infra.outbox.OutboxRelay;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Se habilita por servicio con {@code shared.outbox.enabled=true}: requiere la
 * tabla outbox_events (migración Flyway propia) y @EnableScheduling para el relay.
 */
@AutoConfiguration
@ConditionalOnClass({JdbcTemplate.class, KafkaTemplate.class})
@ConditionalOnProperty(name = "shared.outbox.enabled", havingValue = "true")
public class SharedOutboxAutoConfiguration {

    @Bean
    public OutboxEventWriter outboxEventWriter(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        return new OutboxEventWriter(jdbcTemplate, objectMapper);
    }

    @Bean
    public OutboxRelay outboxRelay(JdbcTemplate jdbcTemplate,
                                   KafkaTemplate<String, Object> kafkaTemplate,
                                   ObjectMapper objectMapper) {
        return new OutboxRelay(jdbcTemplate, kafkaTemplate, objectMapper);
    }
}
