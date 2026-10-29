package com.api.catalog.productcatalogapi.service;

import com.api.catalog.productcatalogapi.domain.Product;
import com.api.catalog.productcatalogapi.dto.ProductDto;
import com.api.catalog.productcatalogapi.exceptions.DuplicateSkuException;
import com.api.catalog.productcatalogapi.exceptions.ProductNotFoundException;
import com.api.catalog.productcatalogapi.mapper.ProductMapper;
import com.api.catalog.productcatalogapi.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductCommandServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductValidationService validationService;

    private ProductCommandService productCommandService;

    @BeforeEach
    void setUp() {
        productCommandService = new ProductCommandService(productRepository, productMapper, validationService);
    }

    @Test
    void createProduct_ShouldReturnProductDto_WhenSkuIsUnique() {
        ProductDto inputDto = new ProductDto();
        inputDto.setSku("SKU-1");
        Product product = new Product();
        Product savedProduct = new Product();
        ProductDto expectedDto = new ProductDto();

        when(productRepository.existsBySku("SKU-1")).thenReturn(false);
        when(productMapper.toEntity(inputDto)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(savedProduct);
        when(productMapper.toDto(savedProduct)).thenReturn(expectedDto);

        ProductDto result = productCommandService.createProduct(inputDto);

        assertSame(expectedDto, result);
        verify(validationService).validateProduct(product);
        verify(productRepository).save(product);
    }

    @Test
    void createProduct_ShouldThrowDuplicateSkuException_WhenSkuAlreadyExists() {
        ProductDto inputDto = new ProductDto();
        inputDto.setSku("SKU-1");

        when(productRepository.existsBySku("SKU-1")).thenReturn(true);

        assertThrows(DuplicateSkuException.class, () -> productCommandService.createProduct(inputDto));
        verifyNoInteractions(productMapper);
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_ShouldReturnUpdatedProductDto_WhenProductExists() {
        Long id = 1L;
        ProductDto inputDto = new ProductDto();
        Product existingProduct = new Product();
        Product savedProduct = new Product();
        ProductDto expectedDto = new ProductDto();

        when(productRepository.findById(id)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(existingProduct)).thenReturn(savedProduct);
        when(productMapper.toDto(savedProduct)).thenReturn(expectedDto);

        ProductDto result = productCommandService.updateProduct(id, inputDto);

        assertSame(expectedDto, result);
        verify(productMapper).updateEntity(existingProduct, inputDto);
        verify(validationService).validateProduct(existingProduct);
        verify(productRepository).save(existingProduct);
    }

    @Test
    void updateProduct_ShouldThrowException_WhenProductNotFound() {
        Long id = 1L;
        ProductDto inputDto = new ProductDto();

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productCommandService.updateProduct(id, inputDto)
        );

        assertEquals("Product not found with id: 1", exception.getMessage());
        verifyNoInteractions(productMapper);
        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteProduct_ShouldDeleteProduct_WhenProductExists() {
        Long id = 1L;
        Product existingProduct = new Product();

        when(productRepository.findById(id)).thenReturn(Optional.of(existingProduct));

        productCommandService.deleteProduct(id);

        verify(productRepository).delete((Product) existingProduct);
    }

    @Test
    void deleteProduct_ShouldThrowException_WhenProductNotFound() {
        Long id = 1L;

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productCommandService.deleteProduct(id));
        verify(productRepository, never()).delete(any(Product.class));
    }
}
