package com.example.orderservice.client;

import java.math.BigDecimal;
import java.util.List;

public record CartSnapshot(
        Long id,
        String userId,
        List<CartItemSnapshot> items,
        Integer totalItems,
        BigDecimal totalAmount
) {
}
