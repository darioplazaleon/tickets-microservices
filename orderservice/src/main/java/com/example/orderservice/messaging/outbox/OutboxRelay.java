package com.example.orderservice.messaging.outbox;

import com.example.orderservice.config.KafkaTracingInterceptor;
import com.example.orderservice.entity.OutboxEvent;
import com.example.orderservice.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxRelay {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 2_000)
    public void publishPending() {
        List<OutboxEvent> pending = outboxEventRepository.findTop100ByPublishedAtIsNullOrderByCreatedAtAsc();

        for (OutboxEvent outbox : pending) {
            try {
                // Se reconstruye el objeto tipado para que JsonSerializer agregue el
                // header __TypeId__ que los consumidores usan para deserializar.
                Object event = objectMapper.readValue(outbox.getPayload(), Class.forName(outbox.getEventType()));

                ProducerRecord<String, Object> record =
                        new ProducerRecord<>(outbox.getTopic(), outbox.getMessageKey(), event);
                if (outbox.getCorrelationId() != null) {
                    record.headers().add(KafkaTracingInterceptor.CORRELATION_ID_KAFKA_HEADER,
                            outbox.getCorrelationId().getBytes(StandardCharsets.UTF_8));
                }

                kafkaTemplate.send(record).get(10, TimeUnit.SECONDS);

                outbox.setPublishedAt(Instant.now());
                outboxEventRepository.save(outbox);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                // Se corta el batch para no publicar fuera de orden; el próximo tick reintenta.
                log.error("Failed to publish outbox event {} to topic {}, will retry",
                        outbox.getId(), outbox.getTopic(), e);
                return;
            }
        }
    }
}
