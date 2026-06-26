package com.example.cartservice.api;

import com.example.cartservice.model.Cart;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long id,
        String userId,
        List<CartItemResponse> items,
        Integer totalItems,
        BigDecimal totalAmount
) {
    public static CartResponse from(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(CartItemResponse::from)
                .toList();
        Integer totalItems = items.stream().map(CartItemResponse::quantity).reduce(0, Integer::sum);
        BigDecimal totalAmount = items.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(cart.getId(), cart.getUserId(), items, totalItems, totalAmount);
    }
}
