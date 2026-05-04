package com.example.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class InventoryRequest {

    @NotNull(message = "Product ID is required ")
    private Long productId;

    @NotBlank(message = "SKU is required")
    private String sku;

    @NotNull(message = "Quantity on hand is required")
    @PositiveOrZero(message = "Quantity on hand must be zero or positive")
    private Integer quantityOnHand;

    private Integer reorderLevel;
}
