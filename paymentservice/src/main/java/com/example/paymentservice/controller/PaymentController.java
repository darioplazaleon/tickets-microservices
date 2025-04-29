package com.example.paymentservice.controller;

import com.example.paymentservice.response.StripeResponse;
import com.example.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/checkout/{orderId}")
    public ResponseEntity<StripeResponse> checkoutOrder(
            @PathVariable UUID orderId) {
        StripeResponse response = paymentService.checkoutProducts(orderId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}


