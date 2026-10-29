package com.api.catalog.productcatalogapi.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product aggregate for the catalog. Reads vastly outnumber writes, so the
 * shape favors a flat, denormalized row (category as an indexed string rather
 * than a joined table) so that list/filter queries can be served by a single
 * covering index without joins.
 */
@Entity
@Table(name = "products")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Validated
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "SKU is required")
    @Column(nullable = false, unique = true, length = 64)
    private String sku;

    @NotBlank(message = "Product name is required")
    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 2000)
    private String description;

    @NotBlank(message = "Category is required")
    @Column(nullable = false, length = 100)
    private String category;

    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price must not be negative")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @NotBlank(message = "Currency is required")
    @Column(nullable = false, length = 3)
    private String currency;

    @NotNull(message = "Stock quantity is required")
    @PositiveOrZero(message = "Stock quantity must not be negative")
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
