package com.api.catalog.productcatalogapi.service;

import com.api.catalog.productcatalogapi.domain.Product;
import com.api.catalog.productcatalogapi.dto.ProductDto;
import com.api.catalog.productcatalogapi.mapper.ProductMapper;
import com.api.catalog.productcatalogapi.repository.ProductCursorSearchEngine;
import com.api.catalog.productcatalogapi.repository.ProductRepository;
import com.api.catalog.productcatalogapi.repository.ProductSpecifications;
import com.api.catalog.productcatalogapi.utils.CursorPageResponse;
import com.api.catalog.productcatalogapi.utils.PageResponse;
import com.api.catalog.productcatalogapi.utils.ProductSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Read path for the catalog. Reads vastly outnumber writes, so every query
 * here is designed to be served by an index:
 * <ul>
 *   <li>{@link #searchProducts} is classic OFFSET/LIMIT paging for UI "page
 *   N of M" experiences; page size is capped to avoid clients requesting
 *   huge, expensive pages.</li>
 *   <li>{@link #searchProductsAfterCursor} is keyset pagination for infinite
 *   scroll / bulk export, where OFFSET cost growing with page depth would be
 *   a problem once the catalog reaches hundreds of thousands of rows.</li>
 * </ul>
 */
@Service
public class ProductQueryService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_CURSOR_LIMIT = 200;
    private static final int DEFAULT_CURSOR_LIMIT = 50;

    private final ProductRepository productRepository;
    private final ProductCursorSearchEngine cursorSearchEngine;
    private final ProductMapper productMapper;

    public ProductQueryService(ProductRepository productRepository,
                                ProductCursorSearchEngine cursorSearchEngine,
                                ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.cursorSearchEngine = cursorSearchEngine;
        this.productMapper = productMapper;
    }

    public PageResponse<ProductDto> searchProducts(ProductSearchCriteria criteria, Integer page, Integer size) {
        int safePage = (page == null || page < 0) ? 0 : page;
        int safeSize = (size == null || size <= 0) ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);

        Specification<Product> spec = ProductSpecifications.fromCriteria(criteria);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.ASC, "id"));

        Page<Product> result = productRepository.findAll(spec, pageable);
        Page<ProductDto> dtoPage = result.map(productMapper::toDto);
        return PageResponse.from(dtoPage);
    }

    public CursorPageResponse<ProductDto> searchProductsAfterCursor(ProductSearchCriteria criteria, Long afterId, Integer limit) {
        int safeLimit = (limit == null || limit <= 0) ? DEFAULT_CURSOR_LIMIT : Math.min(limit, MAX_CURSOR_LIMIT);

        // Fetch one extra row to cheaply know whether another page follows,
        // without a separate COUNT query.
        List<Product> rows = cursorSearchEngine.searchAfter(criteria, afterId, safeLimit + 1);

        boolean hasNext = rows.size() > safeLimit;
        List<Product> page = hasNext ? rows.subList(0, safeLimit) : rows;

        Long nextCursor = page.isEmpty() ? afterId : page.get(page.size() - 1).getId();
        List<ProductDto> dtos = page.stream().map(productMapper::toDto).collect(Collectors.toList());

        return new CursorPageResponse<>(dtos, nextCursor, hasNext);
    }

    public Optional<ProductDto> getProductById(Long id) {
        return productRepository.findById(id).map(productMapper::toDto);
    }
}
