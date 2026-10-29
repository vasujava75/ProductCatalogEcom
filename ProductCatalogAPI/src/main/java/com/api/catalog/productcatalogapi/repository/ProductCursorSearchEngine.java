package com.api.catalog.productcatalogapi.repository;

import com.api.catalog.productcatalogapi.domain.Product;
import com.api.catalog.productcatalogapi.utils.ProductSearchCriteria;

import java.util.List;

/**
 * Keyset ("seek method") pagination, as an alternative to OFFSET-based
 * paging for deep scrolling through a large, mostly-read catalog. Ordered by
 * primary key, which is already indexed, so no extra index is required
 * beyond what filtering already needs.
 */
public interface ProductCursorSearchEngine {

    /**
     * Returns up to {@code limit} products matching the criteria with id
     * greater than {@code afterId}, ordered by id ascending.
     */
    List<Product> searchAfter(ProductSearchCriteria criteria, Long afterId, int limit);
}
