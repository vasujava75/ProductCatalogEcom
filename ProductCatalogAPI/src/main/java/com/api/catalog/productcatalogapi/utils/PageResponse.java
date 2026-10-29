package com.api.catalog.productcatalogapi.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Stable, client-facing envelope for offset-paginated results. Decouples API
 * clients from Spring Data's {@link Page} type so the internal pagination
 * implementation can change without breaking the contract.
 */
@Data
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}
