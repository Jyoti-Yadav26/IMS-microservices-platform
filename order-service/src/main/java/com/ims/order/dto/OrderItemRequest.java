package com.ims.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OrderItemRequest(
        @NotBlank(message = "sku is required") String sku,
        @NotNull @Min(value = 1, message = "quantity must be >= 1") Integer quantity,
        @NotNull @DecimalMin(value = "0.0", message = "unitPrice must be >= 0") BigDecimal unitPrice
) {
}
