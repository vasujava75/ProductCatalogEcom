package com.api.catalog.productcatalogapi.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Keyset ("cursor") pagination envelope. Unlike OFFSET-based paging, the cost
 * of fetching a page does not grow with how deep into the result set the
 * client is, which matters once the catalog holds hundreds of thousands of
 * rows. {@code nextCursor} is the id of the last row returned; pass it back
 * as {@code afterId} to fetch the next page.
 */
@Data
@AllArgsConstructor
public class CursorPageResponse<T> {
    private List<T> content;
    private Long nextCursor;
    private boolean hasNext;
}
