package com.example.orderservice.messaging;

import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.model.ProcessedEvent;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.repository.ProcessedEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Component
public class PaymentEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);

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

    @RetryableTopic(attempts = "3")
    @KafkaListener(topics = "${app.kafka.topics.payment-completed:payment-completed}", groupId = "order-service")
    @Transactional
    public void handlePaymentCompleted(String message) {
        try {
            PaymentCompletedEvent event = objectMapper.readValue(message, PaymentCompletedEvent.class);
            if (processedEventRepository.existsByEventId(event.getEventId())) {
                log.info("Ignoring duplicate payment event eventId={} correlationId={}",
                        event.getEventId(), event.getCorrelationId());
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
            log.info("Order payment event applied orderId={} correlationId={}",
                    event.getOrderId(), event.getCorrelationId());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to process payment event", ex);
        }
    }

    @DltHandler
    public void handlePaymentDlt(String message) {
        log.error("Payment event moved to DLT after retries payload={}", message);
    }
}
