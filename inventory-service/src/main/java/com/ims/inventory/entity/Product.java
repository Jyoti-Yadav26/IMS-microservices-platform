package com.ims.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Row in the inventory database - one product/SKU and its current stock level.
 * {@code version} backs optimistic locking: concurrent {@code reserveStock} calls on the
 * same row will make JPA raise {@link jakarta.persistence.OptimisticLockException} for
 * whichever transaction commits second, so stock can never be oversold by two requests
 * racing each other. The service layer catches that and retries a bounded number of times.
 */
@Entity
@Table(name = "products", uniqueConstraints = @UniqueConstraint(columnNames = "sku"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "reorder_threshold", nullable = false)
    private Integer reorderThreshold;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Version
    private Long version;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public boolean isBelowThreshold() {
        return quantity != null && reorderThreshold != null && quantity <= reorderThreshold;
    }
}
