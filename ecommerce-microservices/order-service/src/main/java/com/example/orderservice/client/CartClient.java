package com.example.orderservice.client;

import com.example.orderservice.exception.BadRequestException;
import com.example.orderservice.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CartClient {

    private final RestClient restClient;

    public CartClient(@Value("${app.services.cart-service-url:http://cart-service:8082}") String baseUrl,
                      RestClient.Builder builder) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public CartSnapshot getCart(String userId) {
        return restClient.get()
                .uri("/api/cart/{userId}", userId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new NotFoundException("Cart not found for user: " + userId);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new BadRequestException("Cart service is unavailable");
                })
                .body(CartSnapshot.class);
    }

    public void clearCart(String userId) {
        restClient.delete()
                .uri("/api/cart/{userId}/items", userId)
                .retrieve()
                .toBodilessEntity();
    }
}
