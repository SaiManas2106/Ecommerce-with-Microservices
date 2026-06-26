package com.example.paymentservice.messaging;

import com.example.paymentservice.model.Payment;
import com.example.paymentservice.model.PaymentStatus;
import com.example.paymentservice.model.ProcessedEvent;
import com.example.paymentservice.repository.PaymentRepository;
import com.example.paymentservice.repository.ProcessedEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    private final ObjectMapper objectMapper;
    private final PaymentRepository paymentRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final PaymentEventProducer paymentEventProducer;

    public OrderEventListener(ObjectMapper objectMapper,
                              PaymentRepository paymentRepository,
                              ProcessedEventRepository processedEventRepository,
                              PaymentEventProducer paymentEventProducer) {
        this.objectMapper = objectMapper;
        this.paymentRepository = paymentRepository;
        this.processedEventRepository = processedEventRepository;
        this.paymentEventProducer = paymentEventProducer;
    }

    @RetryableTopic(attempts = "3")
    @KafkaListener(topics = "${app.kafka.topics.order-created:order-created}", groupId = "payment-service")
    @Transactional
    public void handleOrderCreated(String message) {
        try {
            OrderCreatedEvent event = objectMapper.readValue(message, OrderCreatedEvent.class);
            if (processedEventRepository.existsByEventId(event.getEventId())) {
                log.info("Ignoring duplicate order event eventId={} correlationId={}",
                        event.getEventId(), event.getCorrelationId());
                return;
            }

            Payment payment = createPayment(event);
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setUpdatedAt(Instant.now());
            paymentRepository.save(payment);
            markProcessed(event.getEventId());

            PaymentCompletedEvent completedEvent = new PaymentCompletedEvent(
                    UUID.randomUUID().toString(),
                    "PAYMENT_COMPLETED",
                    event.getCorrelationId(),
                    payment.getOrderId(),
                    payment.getUserId(),
                    payment.getAmount(),
                    payment.getStatus().name(),
                    payment.getUpdatedAt()
            );
            paymentEventProducer.sendPaymentCompletedEvent(completedEvent);
            log.info("Payment completed orderId={} correlationId={}", payment.getOrderId(), event.getCorrelationId());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to process order-created event", ex);
        }
    }

    @DltHandler
    public void handleOrderCreatedDlt(String message) {
        log.error("Order-created event moved to DLT after retries payload={}", message);
    }

    private Payment createPayment(OrderCreatedEvent event) {
        Payment payment = new Payment();
        payment.setOrderId(event.getOrderId());
        payment.setUserId(event.getUserId());
        payment.setAmount(event.getTotalAmount());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(Instant.now());
        payment.setUpdatedAt(Instant.now());
        return paymentRepository.save(payment);
    }

    private void markProcessed(String eventId) {
        ProcessedEvent processedEvent = new ProcessedEvent();
        processedEvent.setEventId(eventId);
        processedEvent.setProcessedAt(Instant.now());
        processedEventRepository.save(processedEvent);
    }
}
