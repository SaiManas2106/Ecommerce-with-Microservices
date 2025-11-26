package com.example.cartservice.controller;

import com.example.cartservice.api.AddCartItemRequest;
import com.example.cartservice.model.Cart;
import com.example.cartservice.model.CartItem;
import com.example.cartservice.repository.CartRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart", description = "Shopping cart APIs")
public class CartController {

    private final CartRepository cartRepository;

    public CartController(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @PostMapping("/{userId}/items")
    @Operation(summary = "Add item to a user's cart")
    public ResponseEntity<Cart> addItem(@PathVariable String userId,
                                        @Valid @RequestBody AddCartItemRequest request) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setUserId(userId);
                    return c;
                });

        CartItem item = new CartItem();
        item.setProductId(request.getProductId());
        item.setQuantity(request.getQuantity());
        item.setCart(cart);

        cart.getItems().add(item);

        Cart saved = cartRepository.save(cart);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get cart for a user")
    public ResponseEntity<Cart> getCart(@PathVariable String userId) {
        return cartRepository.findByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{userId}/items")
    @Operation(summary = "Clear a user's cart")
    public ResponseEntity<Void> clearCart(@PathVariable String userId) {
        return cartRepository.findByUserId(userId)
                .map(cart -> {
                    cart.getItems().clear();
                    cartRepository.save(cart);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
