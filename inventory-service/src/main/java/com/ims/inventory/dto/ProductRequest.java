package com.ims.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "sku is required") String sku,
        @NotBlank(message = "name is required") String name,
        String description,
        @NotNull @DecimalMin(value = "0.0", inclusive = true, message = "price must be >= 0") BigDecimal price,
        @NotNull @Min(value = 0, message = "quantity must be >= 0") Integer quantity,
        @NotNull @Min(value = 0, message = "reorderThreshold must be >= 0") Integer reorderThreshold
) {
}
