package com.api.catalog.productcatalogapi.resource;

import com.api.catalog.productcatalogapi.dto.ProductDto;
import com.api.catalog.productcatalogapi.service.ProductQueryService;
import com.api.catalog.productcatalogapi.utils.CursorPageResponse;
import com.api.catalog.productcatalogapi.utils.PageResponse;
import com.api.catalog.productcatalogapi.utils.ProductSearchCriteria;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/products")
public class ProductQueryController {

    private final ProductQueryService queryService;

    @Autowired
    public ProductQueryController(ProductQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping
    @Operation(
            summary = "List and filter products",
            description = "Lists products filterable by category and price range, with offset-based pagination. " +
                    "Intended for UI pages that show total counts / page numbers."
    )
    public ResponseEntity<PageResponse<ProductDto>> getProducts(
            @Parameter(description = "Exact category match") @RequestParam(required = false) String category,
            @Parameter(description = "Minimum price (inclusive)") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price (inclusive)") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Case-insensitive substring match on name") @RequestParam(required = false) String name,
            @Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Zero-based page index") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Page size, capped at 100") @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        ProductSearchCriteria criteria = new ProductSearchCriteria(category, minPrice, maxPrice, name, active);
        return ResponseEntity.ok(queryService.searchProducts(criteria, page, size));
    }

    @GetMapping("/feed")
    @Operation(
            summary = "Keyset-paginated product feed",
            description = "Cursor-based pagination ordered by id. Avoids the OFFSET cost of deep pages, which " +
                    "matters once the catalog holds hundreds of thousands of products (e.g. bulk export, infinite scroll)."
    )
    public ResponseEntity<CursorPageResponse<ProductDto>> getProductsFeed(
            @Parameter(description = "Exact category match") @RequestParam(required = false) String category,
            @Parameter(description = "Minimum price (inclusive)") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price (inclusive)") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Case-insensitive substring match on name") @RequestParam(required = false) String name,
            @Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Id of the last product seen; omit for the first page") @RequestParam(required = false) Long afterId,
            @Parameter(description = "Page limit, capped at 200") @RequestParam(required = false, defaultValue = "50") Integer limit
    ) {
        ProductSearchCriteria criteria = new ProductSearchCriteria(category, minPrice, maxPrice, name, active);
        return ResponseEntity.ok(queryService.searchProductsAfterCursor(criteria, afterId, limit));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a product by id")
    public ResponseEntity<ProductDto> getProduct(@PathVariable Long id) {
        Optional<ProductDto> product = queryService.getProductById(id);
        return product.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
