package com.example.productservice.controller;

import com.example.productservice.api.InventoryAdjustmentRequest;
import com.example.productservice.api.ProductRequest;
import com.example.productservice.api.ProductResponse;
import com.example.productservice.model.ProductStatus;
import com.example.productservice.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product management APIs")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse saved = productService.create(request);
        URI location = URI.create("/api/products/" + saved.id());
        return ResponseEntity.created(location).body(saved);
    }

    @GetMapping
    @Operation(summary = "List products with optional category and status filters")
    public List<ProductResponse> getAllProducts(@RequestParam(required = false) String category,
                                                @RequestParam(required = false) ProductStatus status) {
        return productService.list(category, status);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a product by id")
    public ProductResponse getProduct(@PathVariable Long id) {
        return productService.get(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product catalog data")
    public ProductResponse updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @PostMapping("/{id}/inventory/reserve")
    @Operation(summary = "Reserve inventory for checkout")
    public ProductResponse reserveInventory(@PathVariable Long id,
                                            @Valid @RequestBody InventoryAdjustmentRequest request) {
        return productService.reserve(id, request);
    }

    @PostMapping("/{id}/inventory/restock")
    @Operation(summary = "Return inventory after cancellation or restock")
    public ProductResponse restockInventory(@PathVariable Long id,
                                            @Valid @RequestBody InventoryAdjustmentRequest request) {
        return productService.restock(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Archive a product")
    public ResponseEntity<Void> archiveProduct(@PathVariable Long id) {
        productService.archive(id);
        return ResponseEntity.noContent().build();
    }
}
