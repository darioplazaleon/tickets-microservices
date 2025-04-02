package com.example.notificationservice.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendEmail(String to, String eventName, String qrCodeImage) throws MessagingException {
        // Decodificar la imagen Base64 a un arreglo de bytes
        byte[] qrImageBytes = Base64.getDecoder().decode(qrCodeImage);
        InputStreamSource imageSource = new ByteArrayResource(qrImageBytes);

        // Crear un mensaje MIME
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true); // true indica que será multipart

        helper.setTo(to);
        helper.setSubject("🎫 Tu ticket para el evento: " + eventName);
        helper.setText("<html><body><h1>¡Gracias por tu compra!</h1>" +
                "<p>Adjuntamos tu código QR para el evento: " + eventName + "</p>" +
                "<p>Por favor, muestra este código en la entrada del evento.</p>" +
                "<img src='cid:qrCodeImage'>" +
                "</body></html>", true);

        // Adjuntar la imagen del QR al correo
        helper.addInline("qrCodeImage", imageSource, "image/png");

        // Enviar el email
        mailSender.send(message);
    }
}
