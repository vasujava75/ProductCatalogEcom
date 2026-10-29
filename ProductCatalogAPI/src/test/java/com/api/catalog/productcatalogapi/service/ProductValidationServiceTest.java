package com.api.catalog.productcatalogapi.service;

import com.api.catalog.productcatalogapi.domain.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductValidationServiceTest {

    private final ProductValidationService validationService = new ProductValidationService();

    private Product validProduct() {
        Product product = new Product();
        product.setSku("SKU-1");
        product.setName("Widget");
        product.setCategory("Electronics");
        product.setPrice(new BigDecimal("9.99"));
        product.setCurrency("USD");
        product.setStockQuantity(10);
        return product;
    }

    @Test
    void shouldAcceptValidProduct() {
        assertDoesNotThrow(() -> validationService.validateProduct(validProduct()));
    }

    @Test
    void shouldRejectNullProduct() {
        assertThrows(IllegalArgumentException.class, () -> validationService.validateProduct(null));
    }

    @Test
    void shouldRejectBlankSku() {
        Product product = validProduct();
        product.setSku(" ");
        assertThrows(IllegalArgumentException.class, () -> validationService.validateProduct(product));
    }

    @Test
    void shouldRejectNegativePrice() {
        Product product = validProduct();
        product.setPrice(new BigDecimal("-1.00"));
        assertThrows(IllegalArgumentException.class, () -> validationService.validateProduct(product));
    }

    @Test
    void shouldRejectInvalidCurrency() {
        Product product = validProduct();
        product.setCurrency("us");
        assertThrows(IllegalArgumentException.class, () -> validationService.validateProduct(product));
    }

    @Test
    void shouldRejectNegativeStock() {
        Product product = validProduct();
        product.setStockQuantity(-5);
        assertThrows(IllegalArgumentException.class, () -> validationService.validateProduct(product));
    }

    @Test
    void shouldRejectBlankCategory() {
        Product product = validProduct();
        product.setCategory("");
        assertThrows(IllegalArgumentException.class, () -> validationService.validateProduct(product));
    }
}
