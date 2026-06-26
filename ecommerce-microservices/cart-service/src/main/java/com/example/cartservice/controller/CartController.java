package com.example.cartservice.controller;

import com.example.cartservice.api.AddCartItemRequest;
import com.example.cartservice.api.CartResponse;
import com.example.cartservice.api.UpdateCartItemRequest;
import com.example.cartservice.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart", description = "Shopping cart APIs")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/{userId}/items")
    @Operation(summary = "Add item to a user's cart")
    public CartResponse addItem(@PathVariable String userId,
                                @Valid @RequestBody AddCartItemRequest request) {
        return cartService.addItem(userId, request);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get cart for a user")
    public CartResponse getCart(@PathVariable String userId) {
        return cartService.getCart(userId);
    }

    @PutMapping("/{userId}/items/{productId}")
    @Operation(summary = "Update a cart item quantity")
    public CartResponse updateItem(@PathVariable String userId,
                                   @PathVariable Long productId,
                                   @Valid @RequestBody UpdateCartItemRequest request) {
        return cartService.updateItem(userId, productId, request);
    }

    @DeleteMapping("/{userId}/items/{productId}")
    @Operation(summary = "Remove one item from a user's cart")
    public CartResponse removeItem(@PathVariable String userId, @PathVariable Long productId) {
        return cartService.removeItem(userId, productId);
    }

    @DeleteMapping("/{userId}/items")
    @Operation(summary = "Clear a user's cart")
    public ResponseEntity<Void> clearCart(@PathVariable String userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
