package com.api.catalog.productcatalogapi.service;

import com.api.catalog.productcatalogapi.domain.Product;
import org.springframework.stereotype.Service;

@Service
public class ProductValidationService {

    public void validateProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (product.getSku() == null || product.getSku().trim().isEmpty()) {
            throw new IllegalArgumentException("SKU is required");
        }
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (product.getCategory() == null || product.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("Category is required");
        }
        if (product.getPrice() == null || product.getPrice().signum() < 0) {
            throw new IllegalArgumentException("Price must not be negative");
        }
        if (product.getCurrency() == null || !product.getCurrency().matches("^[A-Z]{3}$")) {
            throw new IllegalArgumentException("Currency must be a 3-letter ISO code, e.g. USD");
        }
        if (product.getStockQuantity() == null || product.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Stock quantity must not be negative");
        }
    }
}
