package com.example.productservice.api;

import com.example.productservice.model.Product;
import com.example.productservice.model.ProductStatus;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String category,
        String description,
        BigDecimal price,
        Integer stock,
        ProductStatus status
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getCategory(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getStatus()
        );
    }
}
