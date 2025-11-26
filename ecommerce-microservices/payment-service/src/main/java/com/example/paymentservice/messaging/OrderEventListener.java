package com.example.paymentservice.messaging;

import com.example.paymentservice.model.Payment;
import com.example.paymentservice.model.PaymentStatus;
import com.example.paymentservice.model.ProcessedEvent;
import com.example.paymentservice.repository.PaymentRepository;
import com.example.paymentservice.repository.ProcessedEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;

@Component
public class OrderEventListener {

    private final ObjectMapper objectMapper;
    private final PaymentRepository paymentRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final PaymentEventProducer paymentEventProducer;
    private final Random random = new Random();

    public OrderEventListener(ObjectMapper objectMapper,
                              PaymentRepository paymentRepository,
                              ProcessedEventRepository processedEventRepository,
                              PaymentEventProducer paymentEventProducer) {
        this.objectMapper = objectMapper;
        this.paymentRepository = paymentRepository;
        this.processedEventRepository = processedEventRepository;
        this.paymentEventProducer = paymentEventProducer;
    }

    @KafkaListener(topics = "${app.kafka.topics.order-created:order-created}", groupId = "payment-service")
    public void handleOrderCreated(String message) {
        int attempts = 0;
        boolean success = false;

        while (!success && attempts < 3) {
            attempts++;
            try {
                OrderCreatedEvent event = objectMapper.readValue(message, OrderCreatedEvent.class);

                if (processedEventRepository.existsByEventId(event.getEventId())) {
                    // idempotent consumer: ignore duplicates
                    return;
                }

                Payment payment = new Payment();
                payment.setOrderId(event.getOrderId());
                payment.setUserId(event.getUserId());
                payment.setAmount(event.getTotalAmount());
                payment.setStatus(PaymentStatus.PENDING);
                payment.setCreatedAt(Instant.now());
                payment.setUpdatedAt(Instant.now());
                payment = paymentRepository.save(payment);

                // Simulate external payment gateway and occasional transient failure
                if (random.nextDouble() < 0.1) {
                    throw new RuntimeException("Simulated transient payment gateway failure");
                }

                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setUpdatedAt(Instant.now());
                paymentRepository.save(payment);

                ProcessedEvent processedEvent = new ProcessedEvent();
                processedEvent.setEventId(event.getEventId());
                processedEvent.setProcessedAt(Instant.now());
                processedEventRepository.save(processedEvent);

                PaymentCompletedEvent completedEvent = new PaymentCompletedEvent(
                        UUID.randomUUID().toString(),
                        payment.getOrderId(),
                        payment.getUserId(),
                        payment.getAmount(),
                        payment.getStatus().name(),
                        payment.getUpdatedAt()
                );
                paymentEventProducer.sendPaymentCompletedEvent(completedEvent);

                success = true;
            } catch (Exception ex) {
                if (attempts >= 3) {
                    System.err.println("Failed to process order-created event after retries: " + ex.getMessage());
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
