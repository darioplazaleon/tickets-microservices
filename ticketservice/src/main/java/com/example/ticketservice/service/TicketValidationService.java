package com.example.ticketservice.service;

import com.example.ticketservice.entity.TicketOwnership;
import com.example.ticketservice.repository.TicketOwnershipRepository;
import com.example.ticketservice.request.QrMasterPayload;
import com.example.ticketservice.request.QrPayload;
import com.example.ticketservice.response.TicketValidationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketValidationService {

    private final TicketOwnershipRepository ticketOwnershipRepository;
    private final QrGenerator qrGenerator;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public TicketValidationResponse validateQrPayload(String jsonPayload) {

        log.info("[Ticket Service] Validating QR code: {}", jsonPayload);

        try {
            if (jsonPayload.contains("ticketId")) {
                QrPayload qr = objectMapper.readValue(jsonPayload, QrPayload.class);

                if (qrGenerator.isValid(qr)) {
                    return TicketValidationResponse.rejected("Invalid QR code");
                }

                TicketOwnership ticket = ticketOwnershipRepository.findById(qr.ticketId()).orElse(null);

                if (ticket == null) {
                    return TicketValidationResponse.rejected("Ticket not found");
                }

                if (!ticket.getCurrentOwnerId().equals(qr.ownerId())) {
                    return TicketValidationResponse.rejected("Ticket does not belong to you");
                }

                if (ticket.isUsed()) {
                    return TicketValidationResponse.rejected("Ticket already used");
                }

                ticket.setUsed(true);
                ticketOwnershipRepository.save(ticket);

                return TicketValidationResponse.accepted();
            }

            QrMasterPayload payload = objectMapper.readValue(jsonPayload, QrMasterPayload.class);

            if (!qrGenerator.isValidMaster(payload)) {
                return TicketValidationResponse.rejected("Invalid QR code");
            }

            List<TicketOwnership> tickets = ticketOwnershipRepository.findAllById(payload.ticketsIds());

            if (tickets.isEmpty()) {
                return TicketValidationResponse.rejected("No tickets found");
            }

            boolean allSameOwner = tickets.stream()
                    .allMatch(t -> t.getCurrentOwnerId().equals(payload.ownerId()));

            if (!allSameOwner) {
                return TicketValidationResponse.rejected("One or more tickets have been transferred");
            }

            boolean anyUsed = tickets.stream()
                    .anyMatch(TicketOwnership::isUsed);

            if (anyUsed) {
                return TicketValidationResponse.rejected("One or more tickets have already been used");
            }

            for (TicketOwnership ticket : tickets) {
                ticket.setUsed(true);
                ticketOwnershipRepository.save(ticket);
            }

            ticketOwnershipRepository.saveAll(tickets);
            return TicketValidationResponse.accepted();
        } catch (Exception e) {
            log.error("[Ticket Service] Error validating QR code: {}", e.getMessage());
            return TicketValidationResponse.rejected("Error validating QR code");
        }
    }

}
