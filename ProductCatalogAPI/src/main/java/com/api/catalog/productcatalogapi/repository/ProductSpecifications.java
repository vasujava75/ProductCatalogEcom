package com.api.catalog.productcatalogapi.repository;

import com.api.catalog.productcatalogapi.domain.Product;
import com.api.catalog.productcatalogapi.utils.ProductSearchCriteria;
import org.springframework.data.jpa.domain.Specification;

/**
 * Composable {@link Specification} predicates for {@link Product}. Using
 * Spring Data Specifications (rather than hand-rolled Criteria API queries)
 * lets the same predicates back both the paged {@code findAll(spec, pageable)}
 * list endpoint and a plain {@code count(spec)}, and keeps each filter
 * independently unit-testable and reusable.
 */
public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> fromCriteria(ProductSearchCriteria criteria) {
        // Always-true predicate as the base, rather than Specification.where(null)
        // (which Spring Data 4.x now rejects with an assertion), so additional
        // filters can be ANDed on unconditionally.
        Specification<Product> spec = (root, query, cb) -> cb.conjunction();
        if (criteria == null) {
            return spec;
        }

        if (criteria.getCategory() != null && !criteria.getCategory().isBlank()) {
            spec = spec.and(hasCategory(criteria.getCategory()));
        }
        if (criteria.getMinPrice() != null) {
            spec = spec.and(priceGreaterThanOrEqual(criteria.getMinPrice()));
        }
        if (criteria.getMaxPrice() != null) {
            spec = spec.and(priceLessThanOrEqual(criteria.getMaxPrice()));
        }
        if (criteria.getNameContains() != null && !criteria.getNameContains().isBlank()) {
            spec = spec.and(nameContains(criteria.getNameContains()));
        }
        if (criteria.getActive() != null) {
            spec = spec.and(isActive(criteria.getActive()));
        }
        return spec;
    }

    public static Specification<Product> hasCategory(String category) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("category")), category.toLowerCase());
    }

    public static Specification<Product> priceGreaterThanOrEqual(java.math.BigDecimal min) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), min);
    }

    public static Specification<Product> priceLessThanOrEqual(java.math.BigDecimal max) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), max);
    }

    public static Specification<Product> nameContains(String text) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + text.toLowerCase() + "%");
    }

    public static Specification<Product> isActive(boolean active) {
        return (root, query, cb) -> cb.equal(root.get("active"), active);
    }
}
