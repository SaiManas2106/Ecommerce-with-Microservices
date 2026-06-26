package com.example.productservice.repository;

import com.example.productservice.model.Product;
import com.example.productservice.model.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    List<Product> findByStatus(ProductStatus status);

    List<Product> findByCategoryIgnoreCaseAndStatus(String category, ProductStatus status);
}
