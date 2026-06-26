package com.example.orderservice.client;

import com.example.orderservice.exception.BadRequestException;
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

    public void reserveInventory(Long productId, Integer quantity) {
        restClient.post()
                .uri("/api/products/{id}/inventory/reserve", productId)
                .body(new InventoryAdjustmentRequest(quantity))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new BadRequestException("Unable to reserve inventory for product: " + productId);
                })
                .toBodilessEntity();
    }

    public void releaseInventory(Long productId, Integer quantity) {
        restClient.post()
                .uri("/api/products/{id}/inventory/restock", productId)
                .body(new InventoryAdjustmentRequest(quantity))
                .retrieve()
                .toBodilessEntity();
    }
}
