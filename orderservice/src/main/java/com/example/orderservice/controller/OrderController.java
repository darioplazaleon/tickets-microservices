package com.example.orderservice.controller;

import com.example.orderservice.response.OrderDTO;
import com.example.orderservice.response.OrderResponse;
import com.example.orderservice.response.OrderSimple;
import com.example.orderservice.response.OrderSummary;
import com.example.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/order/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID orderId) {
        var order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{orderId}/summary")
    public ResponseEntity<OrderSummary> getOrderSummary(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getSummary(orderId));
    }

    @GetMapping("/my-orders")
    public ResponseEntity<Page<OrderSimple>> getMyOrders(
            @RequestHeader("X-User-ID") UUID customerId,
            Pageable pageable) {
        var orders = orderService.getOrdersByCustomerId(customerId, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/my-orders/{orderId}")
    public ResponseEntity<OrderDTO> getMyOrder(
            @PathVariable UUID orderId,
            @RequestHeader("X-User-ID") UUID customerId) {
        var order = orderService.getCustomerOrderById(orderId, customerId);
        return ResponseEntity.ok(order);
    }
}
