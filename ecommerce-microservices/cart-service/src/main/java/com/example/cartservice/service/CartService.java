package com.example.cartservice.service;

import com.example.cartservice.api.AddCartItemRequest;
import com.example.cartservice.api.CartResponse;
import com.example.cartservice.api.UpdateCartItemRequest;
import com.example.cartservice.client.ProductClient;
import com.example.cartservice.client.ProductSnapshot;
import com.example.cartservice.exception.BadRequestException;
import com.example.cartservice.exception.NotFoundException;
import com.example.cartservice.model.Cart;
import com.example.cartservice.model.CartItem;
import com.example.cartservice.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductClient productClient;

    public CartService(CartRepository cartRepository, ProductClient productClient) {
        this.cartRepository = cartRepository;
        this.productClient = productClient;
    }

    @Transactional
    public CartResponse addItem(String userId, AddCartItemRequest request) {
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> newCart(userId));
        int requestedQuantity = request.getQuantity();
        CartItem item = cart.getItems().stream()
                .filter(existing -> existing.getProductId().equals(request.getProductId()))
                .findFirst()
                .orElseGet(() -> newItem(cart, request.getProductId()));

        requestedQuantity += item.getQuantity() == null ? 0 : item.getQuantity();
        ProductSnapshot product = productClient.getProduct(request.getProductId());
        ensureSellable(product, requestedQuantity);

        item.setProductName(product.name());
        item.setUnitPrice(product.price());
        item.setQuantity(requestedQuantity);
        return CartResponse.from(cartRepository.save(cart));
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(String userId) {
        return cartRepository.findByUserId(userId)
                .map(CartResponse::from)
                .orElseThrow(() -> new NotFoundException("Cart not found for user: " + userId));
    }

    @Transactional
    public CartResponse updateItem(String userId, Long productId, UpdateCartItemRequest request) {
        Cart cart = findCart(userId);
        CartItem item = findItem(cart, productId);
        ProductSnapshot product = productClient.getProduct(productId);
        ensureSellable(product, request.getQuantity());
        item.setProductName(product.name());
        item.setUnitPrice(product.price());
        item.setQuantity(request.getQuantity());
        return CartResponse.from(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse removeItem(String userId, Long productId) {
        Cart cart = findCart(userId);
        CartItem item = findItem(cart, productId);
        cart.getItems().remove(item);
        item.setCart(null);
        return CartResponse.from(cartRepository.save(cart));
    }

    @Transactional
    public void clearCart(String userId) {
        Cart cart = findCart(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private Cart findCart(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Cart not found for user: " + userId));
    }

    private CartItem findItem(Cart cart, Long productId) {
        return cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Product not found in cart: " + productId));
    }

    private Cart newCart(String userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        return cart;
    }

    private CartItem newItem(Cart cart, Long productId) {
        CartItem item = new CartItem();
        item.setProductId(productId);
        item.setQuantity(0);
        item.setCart(cart);
        cart.getItems().add(item);
        return item;
    }

    private void ensureSellable(ProductSnapshot product, int requestedQuantity) {
        if (product == null || !product.canSell(requestedQuantity)) {
            throw new BadRequestException("Product is unavailable or does not have enough stock");
        }
    }
}
