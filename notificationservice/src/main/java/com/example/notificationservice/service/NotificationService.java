package com.example.notificationservice.service;

import com.example.notificationservice.client.CustomerServiceClient;
import com.example.notificationservice.client.EventServiceClient;
import com.example.notificationservice.response.CustomerResponse;
import com.example.notificationservice.response.EventDetailsResponse;
import com.example.shared.events.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final QrService qrService;
    private final EmailService emailService;
    private final EventServiceClient eventServiceClient;
    private final CustomerServiceClient customerServiceClient;

    public void processPaymentSuccess(PaymentSucceededEvent paymentSucceededEvent) {
        EventDetailsResponse eventDetails = eventServiceClient.getEventDetails(
                paymentSucceededEvent.eventId()
        );

        CustomerResponse customer = customerServiceClient.getCustomer(paymentSucceededEvent.userId());

        try {
            String qrCodeImage = qrService.generateQRCode(
                    paymentSucceededEvent,
                    eventDetails
            );

            log.info("[Notification Service] QR code generated for orderId: {}", paymentSucceededEvent.orderId());

            emailService.sendEmail(paymentSucceededEvent, eventDetails, customer, qrCodeImage);
            log.info("[Notification Service] Sending email to userId: {}", paymentSucceededEvent.userId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
