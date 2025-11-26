package com.example.orderservice.service;

import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.repository.OrderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class OrderTimeoutScheduler {

    private final OrderRepository orderRepository;

    public OrderTimeoutScheduler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // Runs every minute and cancels orders that have been pending for more than two minutes
    @Scheduled(fixedDelay = 60000)
    public void cancelStaleOrders() {
        Instant cutoff = Instant.now().minusSeconds(120);
        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);

        for (Order order : pendingOrders) {
            if (order.getCreatedAt() != null && order.getCreatedAt().isBefore(cutoff)) {
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
            }
        }
    }
}
