package com.api.catalog.productcatalogapi.service;

import com.api.catalog.productcatalogapi.domain.Product;
import com.api.catalog.productcatalogapi.dto.ProductDto;
import com.api.catalog.productcatalogapi.mapper.ProductMapper;
import com.api.catalog.productcatalogapi.repository.ProductCursorSearchEngine;
import com.api.catalog.productcatalogapi.repository.ProductRepository;
import com.api.catalog.productcatalogapi.utils.CursorPageResponse;
import com.api.catalog.productcatalogapi.utils.PageResponse;
import com.api.catalog.productcatalogapi.utils.ProductSearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductQueryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductCursorSearchEngine cursorSearchEngine;

    @Mock
    private ProductMapper productMapper;

    private ProductQueryService queryService;

    @BeforeEach
    void setUp() {
        queryService = new ProductQueryService(productRepository, cursorSearchEngine, productMapper);
    }

    @Test
    void searchProducts_ShouldCapPageSizeAtMaximum() {
        Product product = new Product();
        ProductDto dto = new ProductDto();
        Page<Product> page = new PageImpl<>(List.of(product), PageRequest.of(0, 100), 1);

        when(productRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);
        when(productMapper.toDto(product)).thenReturn(dto);

        PageResponse<ProductDto> result = queryService.searchProducts(new ProductSearchCriteria(), 0, 10000);

        assertEquals(1, result.getContent().size());
        assertEquals(100, result.getSize());
    }

    @Test
    void searchProductsAfterCursor_ShouldSetHasNext_WhenMoreRowsExist() {
        Product p1 = new Product();
        p1.setId(1L);
        Product p2 = new Product();
        p2.setId(2L);
        Product p3 = new Product();
        p3.setId(3L);

        when(cursorSearchEngine.searchAfter(any(), any(), eqInt(3))).thenReturn(List.of(p1, p2, p3));
        when(productMapper.toDto(any(Product.class))).thenReturn(new ProductDto());

        CursorPageResponse<ProductDto> result = queryService.searchProductsAfterCursor(new ProductSearchCriteria(), null, 2);

        assertEquals(2, result.getContent().size());
        assertTrue(result.isHasNext());
        assertEquals(2L, result.getNextCursor());
    }

    @Test
    void searchProductsAfterCursor_ShouldSetHasNextFalse_WhenNoMoreRows() {
        Product p1 = new Product();
        p1.setId(1L);

        when(cursorSearchEngine.searchAfter(any(), any(), eqInt(3))).thenReturn(List.of(p1));
        when(productMapper.toDto(any(Product.class))).thenReturn(new ProductDto());

        CursorPageResponse<ProductDto> result = queryService.searchProductsAfterCursor(new ProductSearchCriteria(), null, 2);

        assertEquals(1, result.getContent().size());
        assertFalse(result.isHasNext());
        assertEquals(1L, result.getNextCursor());
    }

    @Test
    void getProductById_ShouldReturnEmpty_WhenNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<ProductDto> result = queryService.getProductById(99L);

        assertTrue(result.isEmpty());
    }

    private int eqInt(int value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }
}
