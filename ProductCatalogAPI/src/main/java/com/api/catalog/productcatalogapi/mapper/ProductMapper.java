package com.api.catalog.productcatalogapi.mapper;

import com.api.catalog.productcatalogapi.domain.Product;
import com.api.catalog.productcatalogapi.dto.ProductDto;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductDto toDto(Product product) {
        if (product == null) return null;

        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setSku(product.getSku());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setCategory(product.getCategory());
        dto.setPrice(product.getPrice());
        dto.setCurrency(product.getCurrency());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setActive(product.getActive());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        return dto;
    }

    public Product toEntity(ProductDto dto) {
        if (dto == null) return null;

        Product product = new Product();
        product.setSku(dto.getSku());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setCategory(dto.getCategory());
        product.setPrice(dto.getPrice());
        product.setCurrency(dto.getCurrency());
        product.setStockQuantity(dto.getStockQuantity());
        product.setActive(dto.getActive() != null ? dto.getActive() : Boolean.TRUE);
        return product;
    }

    /**
     * Applies only the client-mutable fields from the DTO onto an existing,
     * managed entity. SKU is intentionally immutable after creation to keep
     * it a stable external identifier; id/createdAt/version are managed by
     * JPA and never copied from client input.
     */
    public void updateEntity(Product target, ProductDto source) {
        if (source == null || target == null) return;

        if (source.getName() != null) target.setName(source.getName());
        if (source.getDescription() != null) target.setDescription(source.getDescription());
        if (source.getCategory() != null) target.setCategory(source.getCategory());
        if (source.getPrice() != null) target.setPrice(source.getPrice());
        if (source.getCurrency() != null) target.setCurrency(source.getCurrency());
        if (source.getStockQuantity() != null) target.setStockQuantity(source.getStockQuantity());
        if (source.getActive() != null) target.setActive(source.getActive());
    }
}
