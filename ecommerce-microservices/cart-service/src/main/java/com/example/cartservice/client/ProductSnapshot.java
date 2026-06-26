package com.example.cartservice.client;

import java.math.BigDecimal;

public record ProductSnapshot(
        Long id,
        String sku,
        String name,
        String category,
        String description,
        BigDecimal price,
        Integer stock,
        String status
) {
    public boolean canSell(int requestedQuantity) {
        return "ACTIVE".equalsIgnoreCase(status) && stock != null && stock >= requestedQuantity;
    }
}
