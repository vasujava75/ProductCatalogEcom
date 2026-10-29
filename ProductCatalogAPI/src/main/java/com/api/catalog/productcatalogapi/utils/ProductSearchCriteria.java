package com.api.catalog.productcatalogapi.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Filter criteria shared by both the offset-based list endpoint and the
 * keyset ("feed") endpoint. Kept criteria-only (no paging fields) so the same
 * object can back either pagination style.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchCriteria {

    private String category;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String nameContains;
    private Boolean active;
}
