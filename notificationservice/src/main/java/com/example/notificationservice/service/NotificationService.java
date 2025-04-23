package com.example.notificationservice.service;

import com.example.notificationservice.client.CustomerServiceClient;
import com.example.notificationservice.client.EventServiceClient;
import com.example.notificationservice.client.OrderServiceClient;
import com.example.notificationservice.client.TicketServiceClient;
import com.example.notificationservice.response.CustomerResponse;
import com.example.notificationservice.response.EventDetailsResponse;
import com.example.notificationservice.response.OrderSummary;
import com.example.shared.data.TicketData;
import com.example.shared.events.TicketMasterQrEvent;
import com.example.shared.events.TicketQrReadyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final EventServiceClient eventServiceClient;
    private final CustomerServiceClient customerServiceClient;
    private final OrderServiceClient orderServiceClient;
    private final TicketServiceClient ticketServiceClient;

    private final EmailService emailService;

    public void processPaymentSuccess(TicketMasterQrEvent event) {
        try {
            EventDetailsResponse eventDetails = eventServiceClient.getEventDetails(event.eventId());
            CustomerResponse customer = customerServiceClient.getCustomer(event.ownerId());
            OrderSummary orderSummary = orderServiceClient.getOrderDetails(event.orderId());
            emailService.sendEmail(event, eventDetails, customer, orderSummary);
            log.info("[Notification Service] Email sent successfully to {}", customer.email());
        } catch (Exception e) {
            log.error("Error sending email for orderId: {}. Error: {}", event.orderId(), e.getMessage());
        }
    }

    public void processTransferTicket(TicketQrReadyEvent event) {
        try {
            EventDetailsResponse eventDetails = eventServiceClient.getEventDetails(event.eventId());
            CustomerResponse originalOwner = customerServiceClient.getCustomer(event.originalBuyerId());
            CustomerResponse newOwner = customerServiceClient.getCustomer(event.currentOwnerId());
            TicketData ticketData = ticketServiceClient.getTicketServiceUrl(event.ticketId());

            emailService.sendTransferEmail(event, eventDetails, originalOwner, newOwner, ticketData);
            log.info("[Notification Service] Email sent successfully to {}", newOwner.email());
        } catch (Exception e) {
            log.error("Error sending email for orderId: {}. Error: {}", event.orderId(), e.getMessage());
        }
    }

}
