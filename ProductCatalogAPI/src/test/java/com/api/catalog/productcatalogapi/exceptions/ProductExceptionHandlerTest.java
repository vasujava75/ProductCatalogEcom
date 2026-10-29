package com.api.catalog.productcatalogapi.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProductExceptionHandlerTest {

    private final ProductExceptionHandler handler = new ProductExceptionHandler();

    @Test
    void handleProductNotFoundException_ShouldReturnErrorMessage() {
        ProductNotFoundException ex = new ProductNotFoundException("Product not found with id: 42");

        Map<String, String> result = handler.handleProductNotFoundException(ex);

        assertEquals("Product not found with id: 42", result.get("error"));
    }

    @Test
    void handleDuplicateSkuException_ShouldReturnErrorMessage() {
        DuplicateSkuException ex = new DuplicateSkuException("A product with SKU 'SKU-1' already exists");

        Map<String, String> result = handler.handleDuplicateSkuException(ex);

        assertEquals("A product with SKU 'SKU-1' already exists", result.get("error"));
    }

    @Test
    void handleIllegalArgument_ShouldReturnBadRequest() {
        IllegalArgumentException ex = new IllegalArgumentException("Price must not be negative");

        ResponseEntity<Map<String, String>> response = handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Price must not be negative", response.getBody().get("error"));
    }

    @Test
    void handleAll_ShouldReturnInternalServerError() {
        RuntimeException ex = new RuntimeException("Unexpected failure");

        ResponseEntity<Map<String, String>> response = handler.handleAll(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().get("error").contains("Unexpected failure"));
    }
}
