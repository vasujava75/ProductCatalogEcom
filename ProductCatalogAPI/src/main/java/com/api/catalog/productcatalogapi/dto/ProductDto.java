package com.api.catalog.productcatalogapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ProductDto {

    @JsonProperty("id")
    private Long id;

    @NotBlank(message = "SKU is required")
    @Size(max = 64, message = "SKU must not exceed 64 characters")
    @JsonProperty("sku")
    private String sku;

    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    @JsonProperty("name")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @JsonProperty("description")
    private String description;

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    @JsonProperty("category")
    private String category;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price must not be negative")
    @Digits(integer = 10, fraction = 2, message = "Price may have at most 2 decimal places")
    @JsonProperty("price")
    private BigDecimal price;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter ISO code, e.g. USD")
    @JsonProperty("currency")
    private String currency;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity must not be negative")
    @JsonProperty("stockQuantity")
    private Integer stockQuantity;

    @NotNull(message = "Active status is required")
    @JsonProperty("active")
    private Boolean active;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
}
