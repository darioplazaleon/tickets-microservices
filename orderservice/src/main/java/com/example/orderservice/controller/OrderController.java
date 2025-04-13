package com.example.orderservice.controller;

import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.request.PaymentSuccessRequest;
import com.example.orderservice.response.OrderResponse;
import com.example.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PutMapping("/update/success/{orderId}")
    public ResponseEntity<Void> updateOrderStatus(@PathVariable UUID orderId, @RequestBody PaymentSuccessRequest paymentSuccessRequest) {
        orderService.updateSuccessOrder(orderId, paymentSuccessRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/order/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
        var order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }
}
