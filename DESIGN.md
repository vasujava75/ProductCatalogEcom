# Design Notes: Product Catalog Service

This document captures the reasoning behind the design, given the stated
constraints: a catalog of a few hundred thousand products, reads that vastly
outnumber writes, and a requirement to list/filter by category and price
range with pagination.

## 1. Datastore choice: PostgreSQL (relational)

A few hundred thousand rows with a handful of well-known filter dimensions
(category, price, active flag, text search on name) is squarely in
relational territory:

- The schema is simple and mostly flat — no deeply nested or polymorphic
  product structure was specified, so there's little benefit from a document
  store's schema flexibility.
- Range queries (`price BETWEEN ... AND ...`) and equality filters (category)
  are exactly what B-tree indexes are built for; a key-value store would
  force those filters into application code or secondary index tables we'd
  have to maintain ourselves.
- At this scale, a single well-indexed Postgres instance (or a read-replica
  fleet behind it) comfortably serves tens of thousands of reads/sec for
  indexed point/range lookups — no need for the operational overhead of a
  search-engine-only or NoSQL solution.
- Flyway gives us versioned, repeatable schema migrations, which matters once
  the catalog and its indexes evolve over time.

If full-text/fuzzy search across descriptions became a first-class
requirement (not just a `name LIKE '%...%'` filter), the natural evolution is
to add a dedicated search index (e.g. OpenSearch/Elasticsearch) fed by
change-data-capture from Postgres, while Postgres remains the system of
record. That's intentionally out of scope here since the brief only asks for
category/price filtering.

## 2. Data model

`products` is a single, flat table (see `V1__Create_products_table.sql`):

- `category` is a plain indexed `VARCHAR` rather than a normalized foreign
  key to a `categories` table. Categories are a small, slowly changing set in
  most catalogs, and storing them directly on the row avoids a join on the
  hottest read path (list-by-category). The trade-off is that renaming a
  category requires an `UPDATE ... WHERE category = ...` instead of editing
  one row — acceptable given write rarity.
- `price` is `NUMERIC(12,2)`, not `FLOAT`, to avoid floating-point rounding
  errors in money math.
- `sku` has a unique constraint; it's the natural external identifier for a
  product and is immutable after creation (the mapper never overwrites it on
  update).
- `version` (JPA `@Version`) adds optimistic locking so two concurrent
  updates (e.g. two admins editing stock) fail fast with `409 Conflict`
  instead of silently clobbering each other — more important as write
  concurrency grows, even though writes are rare.

### Indexes

- `idx_products_category` — serves category-only filters.
- `idx_products_category_price` — composite index serving the single most
  common query shape: filter by category, then by price range (Postgres can
  also use the leading column alone when price isn't filtered).
- `idx_products_price` — serves price-range queries with no category filter.
- `idx_products_active` (partial index, `WHERE active = TRUE`) — most
  storefront reads only care about active products; a partial index keeps it
  small and fast while costing nothing on the (rare) inactive rows.

## 3. API design (CQRS-style)

Following the same pattern as RecipeMaster, read and write concerns are
split into separate controllers/services:

- `ProductQueryController` / `ProductQueryService` — all `GET` endpoints.
- `ProductCommandController` / `ProductCommandService` — `POST`/`PUT`/`DELETE`.

This isn't a full CQRS architecture (there's one database, one model), but
the separation keeps the read path — which needs to be fast, cacheable, and
heavily indexed — free of the validation/transactional concerns that belong
to the write path, and makes it straightforward to scale/cache them
independently later (e.g. put the query service behind a read replica or a
cache without touching command code).

## 4. Pagination strategy — the core design decision

Two complementary pagination mechanisms are implemented, because "list and
filter with pagination" has two different real usage patterns:

### a) Offset pagination — `GET /api/v1/products?page=&size=`

Classic `LIMIT/OFFSET` via Spring Data's `Pageable`, returning total counts
and page numbers. This is what most storefront/admin UIs need ("page 3 of
120", jump to an arbitrary page). Trade-off: `OFFSET` cost grows linearly
with how deep the page is, because the database still has to scan and
discard all preceding rows. At a few hundred thousand rows this is usually
fine for shallow-to-moderate pages, but deep pages (e.g. page 5,000) get
progressively slower.

To keep this safe at scale:
- Page size is capped (100) so a client can't request an unbounded page.
- All filterable/sortable columns are indexed, so Postgres can use an
  index-only or index range scan rather than a full table scan even before
  it hits the `OFFSET`.

### b) Keyset ("seek") pagination — `GET /api/v1/products/feed?afterId=&limit=`

For use cases like bulk export or infinite-scroll browsing, where a client
walks through the entire filtered result set, OFFSET's linear cost is a real
problem at hundreds of thousands of rows. The `/feed` endpoint instead orders
by the primary key and filters `id > :afterId`, which Postgres can satisfy
with an index seek regardless of how far into the result set the client is.
The cost of each page fetch is therefore roughly constant, not proportional
to depth. The trade-off is that it only supports "next page" iteration, not
jumping to an arbitrary page — which is why both endpoints exist, each
suited to its use case, rather than picking one pagination style globally.

Implementation detail: to avoid a second `COUNT` query to know if another
page exists, the cursor engine fetches `limit + 1` rows and trims the extra
one off, using its presence as the `hasNext` signal.

## 5. Read-heavy optimizations

- HikariCP pool sized for concurrent read traffic (`maximum-pool-size: 20`),
  consistent with "reads vastly outnumber writes".
- Specifications (`ProductSpecifications`) compose predicates only for
  filters that are actually supplied, so an unfiltered "list all" query
  doesn't carry dead `WHERE` clauses.
- `PageResponse`/`CursorPageResponse` are stable, hand-defined envelopes
  (not raw Spring Data `Page` JSON) so the API contract doesn't change if the
  underlying pagination implementation changes.
- Natural next steps, called out in the README, are HTTP/CDN or Redis
  caching for hot category pages and routing query traffic to read replicas
  — both straightforward because queries are isolated behind
  `ProductQueryService`.

## 6. Validation & error handling

- Bean Validation annotations on `ProductDto` catch malformed requests
  (`400`) before they reach the service layer.
- `ProductValidationService` re-validates the mapped entity server-side so
  business rules (e.g. currency format, non-negative price/stock) are
  enforced regardless of client-side correctness.
- `ProductNotFoundException` → `404`, `DuplicateSkuException` → `409`,
  optimistic-lock failures → `409`, all handled centrally in
  `ProductExceptionHandler` so controllers stay thin.
