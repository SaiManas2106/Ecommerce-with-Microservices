package com.example.cartservice.client;

import com.example.cartservice.exception.BadRequestException;
import com.example.cartservice.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ProductClient {

    private final RestClient restClient;

    public ProductClient(@Value("${app.services.product-service-url:http://product-service:8081}") String baseUrl,
                         RestClient.Builder builder) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public ProductSnapshot getProduct(Long productId) {
        return restClient.get()
                .uri("/api/products/{id}", productId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new NotFoundException("Product not found: " + productId);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new BadRequestException("Product service is unavailable");
                })
                .body(ProductSnapshot.class);
    }
}
