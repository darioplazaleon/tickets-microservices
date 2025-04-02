package com.example.notificationservice.service;

import com.example.paymentservice.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final QrService qrService;
    private final EmailService emailService;

    @KafkaListener(topics = "payment-success", groupId = "notification-service")
    public void processPaymentSuccess(PaymentEvent paymentEvent) {
        log.info("Received payment event: {}", paymentEvent);
        System.out.println("Received payment event: " + paymentEvent);

        try {
            String qrCodeImage = qrService.generateQRCode(
                    paymentEvent.orderId(),
                    paymentEvent.customerId(),
                    paymentEvent.eventId(),
                    paymentEvent.ticketCount()
            );

            emailService.sendEmail("darioalessandrop@gmail.com", "Ultra BA", qrCodeImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
