package com.example.orderservice.service;

import com.example.orderservice.api.CheckoutRequest;
import com.example.orderservice.api.CreateOrderRequest;
import com.example.orderservice.api.OrderItemRequest;
import com.example.orderservice.api.OrderResponse;
import com.example.orderservice.client.CartClient;
import com.example.orderservice.client.CartItemSnapshot;
import com.example.orderservice.client.CartSnapshot;
import com.example.orderservice.client.ProductClient;
import com.example.orderservice.exception.BadRequestException;
import com.example.orderservice.exception.NotFoundException;
import com.example.orderservice.messaging.OrderCreatedEvent;
import com.example.orderservice.messaging.OrderEventProducer;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;
    private final CartClient cartClient;
    private final ProductClient productClient;

    public OrderService(OrderRepository orderRepository,
                        OrderEventProducer orderEventProducer,
                        CartClient cartClient,
                        ProductClient productClient) {
        this.orderRepository = orderRepository;
        this.orderEventProducer = orderEventProducer;
        this.cartClient = cartClient;
        this.productClient = productClient;
    }

    @Transactional
    public OrderResponse createFromRequest(CreateOrderRequest request) {
        List<OrderItem> items = request.getItems().stream().map(this::toOrderItem).toList();
        Order saved = persistOrder(request.getUserId(), items, OrderStatus.PENDING);
        publishCreated(saved);
        return OrderResponse.from(saved);
    }

    @Transactional
    public OrderResponse checkout(CheckoutRequest request) {
        CartSnapshot cart = cartClient.getCart(request.getUserId());
        if (cart.items() == null || cart.items().isEmpty()) {
            throw new BadRequestException("Cart is empty for user: " + request.getUserId());
        }

        for (CartItemSnapshot item : cart.items()) {
            productClient.reserveInventory(item.productId(), item.quantity());
        }

        List<OrderItem> items = cart.items().stream().map(this::toOrderItem).toList();
        Order saved = persistOrder(cart.userId(), items, OrderStatus.INVENTORY_RESERVED);
        publishCreated(saved);
        cartClient.clearCart(cart.userId());
        return OrderResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long id) {
        return OrderResponse.from(orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id)));
    }

    private Order persistOrder(String userId, List<OrderItem> items, OrderStatus status) {
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(status);
        order.setCreatedAt(Instant.now());
        items.forEach(item -> item.setOrder(order));
        order.setItems(items);
        order.setTotalAmount(calculateTotal(items));
        return orderRepository.save(order);
    }

    private void publishCreated(Order order) {
        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                order.getId(),
                order.getUserId(),
                order.getTotalAmount(),
                order.getCreatedAt()
        );
        orderEventProducer.sendOrderCreatedEvent(event);
    }

    private BigDecimal calculateTotal(List<OrderItem> items) {
        return items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private OrderItem toOrderItem(OrderItemRequest itemReq) {
        OrderItem item = new OrderItem();
        item.setProductId(itemReq.getProductId());
        item.setQuantity(itemReq.getQuantity());
        item.setPrice(itemReq.getPrice());
        return item;
    }

    private OrderItem toOrderItem(CartItemSnapshot snapshot) {
        OrderItem item = new OrderItem();
        item.setProductId(snapshot.productId());
        item.setProductName(snapshot.productName());
        item.setQuantity(snapshot.quantity());
        item.setPrice(snapshot.unitPrice());
        return item;
    }
}
