package com.example.orderservice.client;

import java.math.BigDecimal;

public record CartItemSnapshot(
        Long id,
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
