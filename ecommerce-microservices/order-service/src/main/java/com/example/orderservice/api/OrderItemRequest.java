package com.example.orderservice.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemRequest {

    @NotNull
    private Long productId;

    @Min(1)
    private Integer quantity;

    @DecimalMin("0.0")
    private BigDecimal price;
}
