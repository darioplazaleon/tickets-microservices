package com.example.paymentservice.messaging.publisher;

import com.example.paymentservice.response.OrderResponse;
import com.example.shared.events.PaymentSucceededEvent;
import com.example.shared.messaging.Topics;
import com.example.shared.messaging.Tracing;
import com.example.shared.records.TicketInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

/**
 * Publica directo a Kafka: paymentservice no tiene base de datos propia, así
 * que no puede usar el outbox transaccional de shared-infra.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final KafkaTemplate<String, PaymentSucceededEvent> kafkaTemplate;

    public void emitPaymentSucceededEvent(OrderResponse order) {
        List<TicketInfo> tickets = order.tickets().stream()
                .map(t -> new TicketInfo(t.ticketType(), t.quantity(), t.unitPrice()))
                .toList();

        PaymentSucceededEvent event = new PaymentSucceededEvent(
                order.orderId(),
                order.bookingId(),
                order.eventId(),
                order.customerId(),
                tickets,
                order.totalPrice(),
                order.correlationId(),
                Instant.now()
        );

        ProducerRecord<String, PaymentSucceededEvent> record = new ProducerRecord<>(
                Topics.PAYMENT_SUCCESS, order.orderId().toString(), event
        );
        record.headers().add(Tracing.USER_ID_KAFKA_HEADER,
                order.customerId().toString().getBytes(StandardCharsets.UTF_8));
        record.headers().add(Tracing.CORRELATION_ID_KAFKA_HEADER,
                event.correlationId().toString().getBytes(StandardCharsets.UTF_8));

        kafkaTemplate.send(record);
        log.info("📤 PaymentSucceededEvent emitido: {}", event.orderId());
    }
}
