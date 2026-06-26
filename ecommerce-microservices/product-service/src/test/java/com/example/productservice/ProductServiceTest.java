package com.example.productservice;

import com.example.productservice.api.InventoryAdjustmentRequest;
import com.example.productservice.api.ProductRequest;
import com.example.productservice.exception.BadRequestException;
import com.example.productservice.model.Product;
import com.example.productservice.model.ProductStatus;
import com.example.productservice.repository.ProductRepository;
import com.example.productservice.service.ProductService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductServiceTest {

    private final ProductRepository productRepository = mock(ProductRepository.class);
    private final ProductService productService = new ProductService(productRepository);

    @Test
    void create_rejectsDuplicateSku() {
        ProductRequest request = request("SKU-1", 10);
        when(productRepository.existsBySku("SKU-1")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("SKU already exists");
    }

    @Test
    void reserve_decrementsStockWhenAvailable() {
        Product product = product("SKU-2", 5);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryAdjustmentRequest request = new InventoryAdjustmentRequest();
        request.setQuantity(3);

        assertThat(productService.reserve(1L, request).stock()).isEqualTo(2);
    }

    @Test
    void reserve_rejectsInsufficientStock() {
        Product product = product("SKU-3", 1);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        InventoryAdjustmentRequest request = new InventoryAdjustmentRequest();
        request.setQuantity(2);

        assertThatThrownBy(() -> productService.reserve(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient stock");
    }

    private ProductRequest request(String sku, int stock) {
        ProductRequest request = new ProductRequest();
        request.setSku(sku);
        request.setName("Backpack");
        request.setCategory("Bags");
        request.setPrice(new BigDecimal("79.99"));
        request.setStock(stock);
        request.setStatus(ProductStatus.ACTIVE);
        return request;
    }

    private Product product(String sku, int stock) {
        Product product = new Product();
        product.setId(1L);
        product.setSku(sku);
        product.setName("Backpack");
        product.setCategory("Bags");
        product.setPrice(new BigDecimal("79.99"));
        product.setStock(stock);
        product.setStatus(ProductStatus.ACTIVE);
        return product;
    }
}
