package com.example.notificationservice.messaging.listener;

import com.example.notificationservice.service.EmailService;
import com.example.notificationservice.service.NotificationService;
import com.example.shared.events.TicketQrReadyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketTransferEventListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "tickets.qr.transfer", groupId = "notification-service")
    public void handleTicketTransfer(TicketQrReadyEvent event) {
        log.info("[Notification Service] Received ticket transfer event for orderId: {}", event.orderId());
        notificationService.processTransferTicket(event);
    }

}
