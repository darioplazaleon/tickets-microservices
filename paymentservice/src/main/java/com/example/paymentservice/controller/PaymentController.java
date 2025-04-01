package com.example.paymentservice.controller;

import com.example.paymentservice.request.ProductRequest;
import com.example.paymentservice.response.StripeResponse;
import com.example.paymentservice.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payment")
public class PaymentController {

    private PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<StripeResponse> checkoutOrder(@RequestBody ProductRequest productRequest) {
        StripeResponse response = paymentService.checkoutProducts(productRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
