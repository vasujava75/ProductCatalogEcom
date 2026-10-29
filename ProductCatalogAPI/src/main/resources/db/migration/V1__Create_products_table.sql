CREATE TABLE products (
                           id BIGSERIAL PRIMARY KEY,
                           sku VARCHAR(64) NOT NULL,
                           name VARCHAR(255) NOT NULL,
                           description VARCHAR(2000),
                           category VARCHAR(100) NOT NULL,
                           price NUMERIC(12, 2) NOT NULL,
                           currency VARCHAR(3) NOT NULL,
                           stock_quantity INT NOT NULL,
                           active BOOLEAN NOT NULL DEFAULT TRUE,
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           version BIGINT NOT NULL DEFAULT 0,

                           CONSTRAINT uq_products_sku UNIQUE (sku),
                           CONSTRAINT chk_price_non_negative CHECK (price >= 0),
                           CONSTRAINT chk_stock_non_negative CHECK (stock_quantity >= 0),
                           CONSTRAINT chk_currency_format CHECK (currency ~ '^[A-Z]{3}$')
);

-- Single-column index to support "list by category" and category-only filters.
CREATE INDEX idx_products_category ON products (category);

-- Composite index to serve the most common catalog browsing query: filter by
-- category then by price range, ordered by price. Postgres can use the
-- leading "category" column alone when price is not filtered.
CREATE INDEX idx_products_category_price ON products (category, price);

-- Supports price-range-only queries (no category filter).
CREATE INDEX idx_products_price ON products (price);

-- Supports the active-products-only filter used by most storefront reads.
CREATE INDEX idx_products_active ON products (active) WHERE active = TRUE;
