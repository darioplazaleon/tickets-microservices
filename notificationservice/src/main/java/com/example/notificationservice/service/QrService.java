package com.example.notificationservice.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Service
public class QrService {

    @Value("${qr.secret.key}")
    private String secretKey;

    public String generateQRCode(UUID orderId, UUID userId, UUID eventId) {
        String timestamp = java.time.Instant.now().toString();

        String data = "orderId=%s&userId=%s&eventId=%s&ticketCount=%d&timestamp=%s\",\n" +
                "orderId, userId, eventId, ticketCount, timestamp";

        String signature = generateSignature(data);

        String qrContent = String.format(
                "{\"orderId\":\"%s\",\"userId\":\"%s\",\"eventId\":\"%s\",\"timestamp\":\"%s\",\"signature\":\"%s\"}",
                orderId, userId, eventId, timestamp, signature
        );

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 300, 300);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "png", outputStream);

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (WriterException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    public String generateSignature(String data) {
        try {
            String ALGORITHM = "HmacSHA256";
            Mac mac = Mac.getInstance(ALGORITHM);
            SecretKey key = new SecretKeySpec(Base64.getDecoder().decode(secretKey), ALGORITHM);
            mac.init(key);

            byte[] hash = mac.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error generating signature", e);
        }
    }
}
