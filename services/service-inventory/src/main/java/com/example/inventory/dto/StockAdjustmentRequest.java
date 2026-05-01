package com.example.inventory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockAdjustmentRequest {

    @NotNull(message = "Quantity adjustment is required")
    private Integer quantityChange;

    private String reason;
}
