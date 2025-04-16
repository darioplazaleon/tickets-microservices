package com.example.notificationservice.service;

import com.example.paymentservice.event.outgoing.PaymentSucceededEvent;
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

    @KafkaListener(topics = "tickets.payment.succeeded", groupId = "notification-service")
    public void processPaymentSuccess(PaymentSucceededEvent paymentSucceededEvent) {
        log.info("Received payment event: {}", paymentSucceededEvent);
        System.out.println("Received payment event: " + paymentSucceededEvent);

        try {
            String qrCodeImage = qrService.generateQRCode(
                    paymentSucceededEvent.orderId(),
                    paymentSucceededEvent.userId(),
                    paymentSucceededEvent.eventId()
            );

            emailService.sendEmail("darioalessandrop@gmail.com", "Ultra BA", qrCodeImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
