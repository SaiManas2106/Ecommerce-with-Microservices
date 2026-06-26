package com.example.orderservice.api;

import com.example.orderservice.model.OrderItem;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal price,
        BigDecimal lineTotal
) {
    public static OrderItemResponse from(OrderItem item) {
        BigDecimal price = item.getPrice() == null ? BigDecimal.ZERO : item.getPrice();
        int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
        return new OrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                quantity,
                price,
                price.multiply(BigDecimal.valueOf(quantity))
        );
    }
}
