package com.example.orderservice.controller;

import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.response.OrderResponse;
import com.example.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PutMapping("/order/{id}/update/{status}")
    public ResponseEntity<Void> updateOrderStatus(@PathVariable Long id, @PathVariable OrderStatus status) {
        orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/order/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        var order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }
}
