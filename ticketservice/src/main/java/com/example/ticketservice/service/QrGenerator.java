package com.example.ticketservice.service;

import com.example.ticketservice.request.QrMasterPayload;
import com.example.ticketservice.request.QrPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class QrGenerator {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${qr.secret.key}")
    private String secretKey;

    private String generateSignature(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(Base64.getDecoder().decode(secretKey), "HmacSHA256");
            mac.init(key);
            byte[] hmac = mac.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(hmac);
        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC signature", e);
        }
    }

    private String generateQrBase64(String json) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(json, BarcodeFormat.QR_CODE, 300, 300);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "png", outputStream);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Error generating QR code", e);
        }
    }

    public String generateForTicket(UUID ticketId, UUID ownerId, UUID eventId) {
        String timestamp = Instant.now().toString();

        String raw = "ticketId=%s&ownerId=%s&eventId=%s&timestamp=%s"
                .formatted(ticketId, ownerId, eventId, timestamp);

        String signature = generateSignature(raw);

        QrPayload payload = new QrPayload(
                ticketId,
                ownerId,
                eventId,
                timestamp,
                signature
        );

        try {
            String json = objectMapper.writeValueAsString(payload);
            return generateQrBase64(json);
        } catch (Exception e) {
            throw new RuntimeException("Error generating QR code", e);
        }
    }

    public QrMasterPayload generateQrPayload(UUID orderId, UUID ownerId, List<UUID> ticketIds) {
        String timestamp = Instant.now().toString();
        String raw = "orderId=%s&ownerId=%s&ticketIds=%s&timestamp=%s"
                .formatted(orderId, ownerId, String.join(",", ticketIds.stream().map(UUID::toString).toList()), timestamp);

        String signature = generateSignature(raw);
        return new QrMasterPayload(orderId, ownerId, ticketIds, timestamp, signature);
    }

    public String generateMasterQrBase64(QrMasterPayload qrMasterPayload) {
        try {
            String json = objectMapper.writeValueAsString(qrMasterPayload);
            return generateQrBase64(json);
        } catch (Exception e) {
            throw new RuntimeException("Error generating QR code", e);
        }
    }
}
