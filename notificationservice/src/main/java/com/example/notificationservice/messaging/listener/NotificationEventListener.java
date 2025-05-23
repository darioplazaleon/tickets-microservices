package com.example.notificationservice.messaging.listener;

import com.example.notificationservice.service.NotificationService;
import com.example.shared.events.TicketMasterQrEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "tickets.qr.ready", groupId = "notification-service")
    private void handleTicketMasterQrReady (TicketMasterQrEvent event) {
        log.info("[Notification Service] Received payment succeeded event for orderId: {}", event.orderId());
        notificationService.processPaymentSuccess(event);
        log.info("[Notification Service] Processed Payment Succeeded event for orderId: {}", event.orderId());
    }
}
