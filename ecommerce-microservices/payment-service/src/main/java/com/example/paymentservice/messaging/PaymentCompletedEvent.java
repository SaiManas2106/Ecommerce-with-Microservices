package com.example.paymentservice.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedEvent {

    private String eventId;
    private String eventType;
    private String correlationId;
    private Long orderId;
    private String userId;
    private BigDecimal amount;
    private String status;
    private Instant completedAt;
}
