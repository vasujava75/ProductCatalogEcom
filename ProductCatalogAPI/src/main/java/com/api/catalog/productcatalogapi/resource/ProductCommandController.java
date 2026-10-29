package com.api.catalog.productcatalogapi.resource;

import com.api.catalog.productcatalogapi.dto.ProductDto;
import com.api.catalog.productcatalogapi.service.ProductCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
public class ProductCommandController {

    private final ProductCommandService productCommandService;

    @Autowired
    public ProductCommandController(ProductCommandService commandService) {
        this.productCommandService = commandService;
    }

    @PostMapping
    @Operation(summary = "Create a new product")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "SKU already exists")
    })
    public ResponseEntity<ProductDto> createProduct(@RequestBody @Valid ProductDto productDto) {
        ProductDto createdProduct = productCommandService.createProduct(productDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "409", description = "Product was modified concurrently")
    })
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, @RequestBody ProductDto productDto) {
        ProductDto updatedProduct = productCommandService.updateProduct(id, productDto);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product by id")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productCommandService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
