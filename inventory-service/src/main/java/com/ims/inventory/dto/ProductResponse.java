package com.ims.inventory.dto;

import com.ims.inventory.entity.Product;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        Integer quantity,
        Integer reorderThreshold,
        boolean lowStock
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity(),
                product.getReorderThreshold(),
                product.isBelowThreshold()
        );
    }
}
