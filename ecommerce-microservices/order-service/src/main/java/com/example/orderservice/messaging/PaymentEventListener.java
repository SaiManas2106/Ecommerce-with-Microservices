package com.example.orderservice.messaging;

import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.model.ProcessedEvent;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.repository.ProcessedEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class PaymentEventListener {

    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;
    private final ProcessedEventRepository processedEventRepository;

    public PaymentEventListener(ObjectMapper objectMapper,
                                OrderRepository orderRepository,
                                ProcessedEventRepository processedEventRepository) {
        this.objectMapper = objectMapper;
        this.orderRepository = orderRepository;
        this.processedEventRepository = processedEventRepository;
    }

    @KafkaListener(topics = "${app.kafka.topics.payment-completed:payment-completed}", groupId = "order-service")
    public void handlePaymentCompleted(String message) {
        int attempts = 0;
        boolean success = false;

        while (!success && attempts < 3) {
            attempts++;
            try {
                PaymentCompletedEvent event = objectMapper.readValue(message, PaymentCompletedEvent.class);

                if (processedEventRepository.existsByEventId(event.getEventId())) {
                    // idempotent consumer: ignore duplicates
                    return;
                }

                Optional<Order> optionalOrder = orderRepository.findById(event.getOrderId());
                optionalOrder.ifPresent(order -> {
                    if ("COMPLETED".equalsIgnoreCase(event.getStatus())) {
                        order.setStatus(OrderStatus.COMPLETED);
                    } else {
                        order.setStatus(OrderStatus.FAILED);
                    }
                    orderRepository.save(order);
                });

                ProcessedEvent processedEvent = new ProcessedEvent();
                processedEvent.setEventId(event.getEventId());
                processedEvent.setProcessedAt(Instant.now());
                processedEventRepository.save(processedEvent);

                success = true;
            } catch (Exception ex) {
                if (attempts >= 3) {
                    System.err.println("Failed to process payment event after retries: " + ex.getMessage());
                } else {
                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }
    }
}
