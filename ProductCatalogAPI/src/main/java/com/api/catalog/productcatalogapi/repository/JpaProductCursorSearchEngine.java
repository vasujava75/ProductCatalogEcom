package com.api.catalog.productcatalogapi.repository;

import com.api.catalog.productcatalogapi.domain.Product;
import com.api.catalog.productcatalogapi.utils.ProductSearchCriteria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JpaProductCursorSearchEngine implements ProductCursorSearchEngine {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Product> searchAfter(ProductSearchCriteria criteria, Long afterId, int limit) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> product = query.from(Product.class);

        List<Predicate> predicates = new ArrayList<>();

        if (afterId != null) {
            predicates.add(cb.greaterThan(product.get("id"), afterId));
        }
        if (criteria != null) {
            if (criteria.getCategory() != null && !criteria.getCategory().isBlank()) {
                predicates.add(cb.equal(cb.lower(product.get("category")), criteria.getCategory().toLowerCase()));
            }
            if (criteria.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(product.get("price"), criteria.getMinPrice()));
            }
            if (criteria.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(product.get("price"), criteria.getMaxPrice()));
            }
            if (criteria.getNameContains() != null && !criteria.getNameContains().isBlank()) {
                predicates.add(cb.like(cb.lower(product.get("name")), "%" + criteria.getNameContains().toLowerCase() + "%"));
            }
            if (criteria.getActive() != null) {
                predicates.add(cb.equal(product.get("active"), criteria.getActive()));
            }
        }

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.asc(product.get("id")));

        TypedQuery<Product> typedQuery = entityManager.createQuery(query);
        typedQuery.setMaxResults(limit);
        return typedQuery.getResultList();
    }
}
