package com.example.eventservice;

import com.example.eventservice.entity.Event;
import com.example.eventservice.entity.EventStatus;
import com.example.eventservice.entity.TicketType;
import com.example.eventservice.exception.InsufficientCapacityException;
import com.example.eventservice.repository.EventRepository;
import com.example.eventservice.repository.TicketTypeRepository;
import com.example.eventservice.request.ReserveTicketRequest;
import com.example.eventservice.service.TicketTypeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integración con Postgres real (Testcontainers): valida que el locking
 * optimista + retry de reserveTickets nunca sobrevenda bajo concurrencia.
 * H2/mocks no reproducen los choques de @Version reales.
 */
@SpringBootTest
@Testcontainers
class TicketReservationConcurrencyTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        // En dev el schema "event" lo crea el init script de Docker; acá lo crea Flyway.
        registry.add("spring.datasource.url",
                () -> postgres.getJdbcUrl() + (postgres.getJdbcUrl().contains("?") ? "&" : "?") + "currentSchema=event");
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.create-schemas", () -> "true");
        // No hay broker Kafka en este test.
        registry.add("spring.kafka.listener.auto-startup", () -> "false");
    }

    @Autowired
    private TicketTypeService ticketTypeService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Test
    void concurrentReservationsNeverOversellCapacity() throws Exception {
        Event event = eventRepository.save(Event.builder()
                .name("Concurrency Test Event")
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .status(EventStatus.UPCOMING)
                .createdByUserId(UUID.randomUUID())
                .build());

        int capacity = 10;
        ticketTypeRepository.save(TicketType.builder()
                .name("GENERAL")
                .capacity(capacity)
                .price(BigDecimal.TEN)
                .event(event)
                .build());

        int attempts = 20;
        ExecutorService pool = Executors.newFixedThreadPool(attempts);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successes = new AtomicInteger();

        for (int i = 0; i < attempts; i++) {
            pool.submit(() -> {
                try {
                    start.await();
                    ticketTypeService.reserveTickets(event.getId(), new ReserveTicketRequest("GENERAL", 1));
                    successes.incrementAndGet();
                } catch (InsufficientCapacityException | ObjectOptimisticLockingFailureException
                         | InterruptedException ignored) {
                    // sin capacidad o retry agotado: el intento simplemente no reserva
                }
            });
        }

        start.countDown();
        pool.shutdown();
        assertThat(pool.awaitTermination(60, TimeUnit.SECONDS)).isTrue();

        TicketType result = ticketTypeRepository
                .findByEventIdAndNameIgnoreCase(event.getId(), "GENERAL")
                .orElseThrow();

        // Invariante central: lo reservado coincide con los intentos exitosos
        // y jamás supera la capacidad.
        assertThat(result.getReserved()).isEqualTo(successes.get());
        assertThat(result.getReserved()).isLessThanOrEqualTo(capacity);
        assertThat(successes.get()).isGreaterThan(0);
    }
}
