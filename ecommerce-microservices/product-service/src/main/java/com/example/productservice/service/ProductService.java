package com.example.productservice.service;

import com.example.productservice.api.InventoryAdjustmentRequest;
import com.example.productservice.api.ProductRequest;
import com.example.productservice.api.ProductResponse;
import com.example.productservice.exception.BadRequestException;
import com.example.productservice.exception.NotFoundException;
import com.example.productservice.model.Product;
import com.example.productservice.model.ProductStatus;
import com.example.productservice.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new BadRequestException("SKU already exists: " + request.getSku());
        }

        Product product = new Product();
        apply(product, request);
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> list(String category, ProductStatus status) {
        ProductStatus effectiveStatus = status == null ? ProductStatus.ACTIVE : status;
        List<Product> products = category == null || category.isBlank()
                ? productRepository.findByStatus(effectiveStatus)
                : productRepository.findByCategoryIgnoreCaseAndStatus(category, effectiveStatus);
        return products.stream().map(ProductResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return ProductResponse.from(findProduct(id));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findProduct(id);
        productRepository.findBySku(request.getSku())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BadRequestException("SKU already exists: " + request.getSku());
                });

        apply(product, request);
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public ProductResponse reserve(Long id, InventoryAdjustmentRequest request) {
        Product product = findProduct(id);
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BadRequestException("Product is not active: " + product.getSku());
        }
        if (product.getStock() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock for SKU " + product.getSku());
        }
        product.setStock(product.getStock() - request.getQuantity());
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public ProductResponse restock(Long id, InventoryAdjustmentRequest request) {
        Product product = findProduct(id);
        product.setStock(product.getStock() + request.getQuantity());
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public void archive(Long id) {
        Product product = findProduct(id);
        product.setStatus(ProductStatus.ARCHIVED);
        productRepository.save(product);
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
    }

    private void apply(Product product, ProductRequest request) {
        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setStatus(request.getStatus() == null ? ProductStatus.ACTIVE : request.getStatus());
    }
}
