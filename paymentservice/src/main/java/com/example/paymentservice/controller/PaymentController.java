package com.example.paymentservice.controller;

import com.example.paymentservice.request.ProductRequest;
import com.example.paymentservice.response.StripeResponse;
import com.example.paymentservice.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payment")
public class PaymentController {

    private PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/checkout/{orderId}")
    public ResponseEntity<StripeResponse> checkoutOrder(@PathVariable Long orderId) {
        StripeResponse response = paymentService.checkoutProducts(orderId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
