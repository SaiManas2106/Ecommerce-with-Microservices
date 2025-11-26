package com.example.orderservice.controller;

import com.example.orderservice.api.CreateOrderRequest;
import com.example.orderservice.api.OrderItemRequest;
import com.example.orderservice.messaging.OrderCreatedEvent;
import com.example.orderservice.messaging.OrderEventProducer;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.repository.OrderRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Order management APIs")
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;

    public OrderController(OrderRepository orderRepository,
                           OrderEventProducer orderEventProducer) {
        this.orderRepository = orderRepository;
        this.orderEventProducer = orderEventProducer;
    }

    @PostMapping
    @Operation(summary = "Create a new order")
    public ResponseEntity<Order> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(Instant.now());

        List<OrderItem> items = request.getItems().stream().map(this::toOrderItem).toList();
        items.forEach(item -> item.setOrder(order));
        order.setItems(items);

        BigDecimal total = items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                saved.getId(),
                saved.getUserId(),
                saved.getTotalAmount(),
                saved.getCreatedAt()
        );
        orderEventProducer.sendOrderCreatedEvent(event);

        URI location = URI.create("/api/orders/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an order by id")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private OrderItem toOrderItem(OrderItemRequest itemReq) {
        OrderItem item = new OrderItem();
        item.setProductId(itemReq.getProductId());
        item.setQuantity(itemReq.getQuantity());
        item.setPrice(itemReq.getPrice());
        return item;
    }
}
