package com.example.cartservice.api;

import com.example.cartservice.model.CartItem;

import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
    public static CartItemResponse from(CartItem item) {
        BigDecimal unitPrice = item.getUnitPrice() == null ? BigDecimal.ZERO : item.getUnitPrice();
        Integer quantity = item.getQuantity() == null ? 0 : item.getQuantity();
        return new CartItemResponse(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                quantity,
                unitPrice,
                unitPrice.multiply(BigDecimal.valueOf(quantity))
        );
    }
}
