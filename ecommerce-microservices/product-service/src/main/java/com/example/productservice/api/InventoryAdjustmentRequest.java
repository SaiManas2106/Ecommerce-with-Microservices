package com.example.productservice.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryAdjustmentRequest {

    @NotNull
    @Min(1)
    private Integer quantity;
}
