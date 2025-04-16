package com.example.notificationservice.service;

import com.example.notificationservice.response.CustomerResponse;
import com.example.notificationservice.response.EventDetailsResponse;
import com.example.shared.events.PaymentSucceededEvent;
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

    public void sendEmail(PaymentSucceededEvent event, EventDetailsResponse eventDetails, CustomerResponse customer, String qrCodeImage) throws MessagingException {

        byte[] qrImageBytes = Base64.getDecoder().decode(qrCodeImage);
        InputStreamSource imageSource = new ByteArrayResource(qrImageBytes);

        // Crear un mensaje MIME
        String eventName = eventDetails.name();
        String venue = eventDetails.venueName() + " - " + eventDetails.venueAddress();
        String dateFormatted = eventDetails.startDate().format(DateTimeFormatter.ofPattern("EEEE dd/MM/yyyy - HH:mm").withLocale(Locale.forLanguageTag("es-AR")));

        String ticketSummary = event.tickets().stream()
                .map(t -> "<li>" + t.ticketType() + "x" + t.quantity() + "</li>")
                .collect(Collectors.joining());

        String htmlContent = """
                 <html>
                 <body style="font-family: Arial, sans-serif;">
                     <h2>¡Gracias por tu compra! 🎉</h2>
                     <p>Tu orden ha sido procesada con éxito.</p>
                    \s
                     <h3>📅 Evento: %s</h3>
                     <p><strong>Fecha:</strong> %s</p>
                     <p><strong>Lugar:</strong> %s</p>
                
                     <h4>🎟️ Entradas:</h4>
                     <ul>%s</ul>
                
                     <p><strong>Total:</strong> $%s</p>
                
                     <p>Mostrá este código QR en la entrada del evento:</p>
                     <img src='cid:qrCodeImage' alt='QR Code' />
                
                     <hr>
                     <p style='font-size: small;'>Este email fue generado automáticamente. No respondas a este mensaje.</p>
                 </body>
                 </html>
                \s""".formatted(
                eventName,
                dateFormatted,
                venue,
                ticketSummary,
                event.totalPrice()
        );

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true); // true indica que será multipart

        helper.setTo(customer.email());
        helper.setSubject("🎫 Tu ticket para el evento: " + eventName);
        helper.setText(htmlContent, true);
        helper.addInline("qrCodeImage", imageSource, "image/png");


        mailSender.send(message);
    }
}
