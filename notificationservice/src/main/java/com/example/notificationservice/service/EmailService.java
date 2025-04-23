package com.example.notificationservice.service;

import com.example.notificationservice.response.CustomerResponse;
import com.example.notificationservice.response.EventDetailsResponse;
import com.example.notificationservice.response.OrderSummary;
import com.example.shared.data.TicketData;
import com.example.shared.events.TicketMasterQrEvent;
import com.example.shared.events.TicketQrReadyEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendEmail(TicketMasterQrEvent event, EventDetailsResponse eventDetails, CustomerResponse customer, OrderSummary order) throws MessagingException {
        String eventName = eventDetails.event();
        String venue = eventDetails.venueName() + " - " + eventDetails.venueAddress();
        String dateFormatted = eventDetails.startDate().format(DateTimeFormatter.ofPattern("EEEE dd/MM/yyyy - HH:mm").withLocale(Locale.forLanguageTag("es-AR")));

        String ticketSummary = order.tickets().stream().map(t -> "<li>" + t.ticketType() + "x" + t.quantity() + "</li>").collect(Collectors.joining());

        String htmlContent = """
                 <html>
                 <head>
                    <meta charset="UTF-8">
                 </head>
                 <body style="font-family: Arial, sans-serif;">
                     <h2>¡Gracias por tu compra! 🎉</h2>
                     <p>Tu orden ha sido procesada con éxito.</p>
                    \s
                     <h3>📅 📅 Evento: %s</h3>
                     <p><strong>Fecha:</strong> %s</p>
                     <p><strong>Lugar:</strong> %s</p>
                
                     <h4>🎟️ Entradas:</h4>
                     <ul>%s</ul>
                
                     <p><strong>Total:</strong> $%s</p>
                
                     <p>Mostrá este código QR en la entrada del evento:</p>
                     <img src='cid:qrCodeImage' alt='QR Code' />
                
                     <hr>
                     <p style='font-size: small;'>Una vez transferido alguno de los tickets el QR no puede ser utilizado.</p>
                     <p style='font-size: small;'>Este email fue generado automáticamente. No respondas a este mensaje.</p>
                 </body>
                 </html>
                \s""".formatted(eventName, dateFormatted, venue, ticketSummary, order.totalPrice());

        String subject = "🎫 Tu ticket para el evento: " + eventName;

        sendQrEmail(event.qrBase64(), customer.email(), htmlContent, subject);

    }

    public void sendTransferEmail(TicketQrReadyEvent event, EventDetailsResponse eventDetails, CustomerResponse originalOwner,
                                  CustomerResponse currentOwner, TicketData ticketData) throws MessagingException {

        String eventName = eventDetails.event();
        String venue = eventDetails.venueName() + " - " + eventDetails.venueAddress();
        String dateFormatted = eventDetails.startDate().format(DateTimeFormatter.ofPattern("EEEE dd/MM/yyyy - HH:mm").withLocale(Locale.forLanguageTag("es-AR")));

        String ticketSummary =  "<li>" + ticketData.ticketType() + " x " + 1 + "</li>";

        String originalOwnerName = originalOwner.fullName();

        String htmlContent = """
                 <html>
                 <head>
                    <meta charset="UTF-8">
                 </head>
                 <body style="font-family: Arial, sans-serif;">
                     <h2>¡Transferencia de ticket exitosa! 🎉</h2>
                     <p>%s, te han transferido un ticket.</p>
                    \s
                     <h3>📅 📅 Evento: %s</h3>
                     <p><strong>Fecha:</strong> %s</p>
                     <p><strong>Lugar:</strong> %s</p>
                
                     <h4>🎟️ Entradas:</h4>
                     <ul>%s</ul>
        
                
                     <p>Mostrá este código QR en la entrada del evento:</p>
                     <img src='cid:qrCodeImage' alt='QR Code' />
                
                     <hr>
                     <p style='font-size: small;'>Una vez transferido alguno de los tickets el QR no puede ser utilizado.</p>
                     <p style='font-size: small;'>Este email fue generado automáticamente. No respondas a este mensaje.</p>
                 </body>
                 </html>
                \s""".formatted(originalOwnerName, eventName, dateFormatted, venue, ticketSummary);

        String subject = "🎫 Transferencia de ticket para el evento: " + eventName;

        sendQrEmail(event.qrBase64(), currentOwner.email(), htmlContent, subject);

    }

    private void sendQrEmail(String base64, String to, String htmlContent, String subject) throws MessagingException {
        byte[] qrImageBytes = Base64.getDecoder().decode(base64);
        InputStreamSource imageSource = new ByteArrayResource(qrImageBytes);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        helper.addInline("qrCodeImage", imageSource, "image/png");

        mailSender.send(message);
    }
}
