package com.example.orderservice.controller;

import com.example.orderservice.api.CheckoutRequest;
import com.example.orderservice.api.CreateOrderRequest;
import com.example.orderservice.api.OrderResponse;
import com.example.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Order management APIs")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Create a new order from explicit line items")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse saved = orderService.createFromRequest(request);
        URI location = URI.create("/api/orders/" + saved.id());
        return ResponseEntity.created(location).body(saved);
    }

    @PostMapping("/checkout")
    @Operation(summary = "Checkout a user's cart and reserve inventory")
    public ResponseEntity<OrderResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        OrderResponse saved = orderService.checkout(request);
        URI location = URI.create("/api/orders/" + saved.id());
        return ResponseEntity.created(location).body(saved);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an order by id")
    public OrderResponse getOrder(@PathVariable Long id) {
        return orderService.get(id);
    }
}
