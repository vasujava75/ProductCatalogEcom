# Product Catalog API

A RESTful e-commerce product catalog service built with Spring Boot, following the same multi-module layout and CQRS (Command/Query Responsibility Segregation) style as the RecipeMaster project.

## 🚀 Technologies Used

- **Java 21** - Programming language
- **Spring Boot 4.x** - Application framework
- **Spring Data JPA** (with `JpaSpecificationExecutor`) - Data persistence and dynamic filtering
- **Spring Web MVC** - RESTful web services
- **PostgreSQL** - Primary datastore
- **Flyway** - Version-controlled database migrations
- **Maven** (multi-module) - Build automation
- **Lombok** - Reduces entity/DTO boilerplate
- **SpringDoc OpenAPI** - API documentation (`/swagger-ui.html`)
- **Spring Boot Validation** - Request validation
- **Testcontainers** - Postgres-backed integration tests
- **Spring Boot Actuator + Micrometer/Prometheus** - Health and metrics

## 📦 Modules

- `ProductCatalogAPI` – the Spring Boot service (domain, REST API, persistence, tests)
- `ProductCatalogDocker` – Dockerfile + docker-compose for local/OpenShift packaging
- `ProductCatalogConf` – Postman collection and OpenAPI spec artifacts

## 📋 Features

- Create, read, update, and delete products (`/api/v1/products`)
- Filter by **category** and **price range**, with **offset pagination** (`page`/`size`) for UI-style browsing
- A keyset/cursor **feed** endpoint (`/api/v1/products/feed`) for deep pagination over large result sets (bulk export, infinite scroll) without OFFSET cost
- Optimistic locking (`@Version`) to protect concurrent updates
- SKU uniqueness enforced at the DB (`UNIQUE` constraint) and surfaced as `409 Conflict`
- Flyway-managed schema with indexes tuned for the catalog's read-heavy filter patterns

See [`DESIGN.md`](./DESIGN.md) for the full design rationale (data model, indexing, pagination trade-offs, and scaling considerations for hundreds of thousands of products).

## 🚦 Getting Started

### Prerequisites

- Java 21, Maven 3.6+
- Docker installed and running (for Testcontainers and local docker-compose)

### Run tests + local stack

```bash
cd ProductCatalogAPI
./mvnw clean verify         # unit + Testcontainers integration tests

cd ../ProductCatalogDocker
../ProductCatalogAPI/mvnw -f ../ProductCatalogAPI/pom.xml clean package -DskipTests
mvn clean install -P local  # builds the image and runs postgres + api via docker-compose
```

API docs: `http://localhost:8080/swagger-ui.html`
Health: `http://localhost:8080/actuator/health`

> **Note on ports:** `docker-compose.yml` publishes Postgres on host port `5433` (not the default `5432`) to avoid
> colliding with any other Postgres instance you may already have running locally. Override it with
> `POSTGRES_HOST_PORT=<port> docker compose ... up -d` if `5433` is also taken. The app container always talks to
> Postgres over the internal Docker network as `postgres:5432`, so this only affects host-side connections
> (e.g. `psql`, DBeaver, or the OpenAPI-generation profile below).

### Generate the OpenAPI spec file

To produce a standalone `openapi.yaml` (e.g. for API clients, docs sites, or committing to `ProductCatalogConf`), run the
`generate-openapi-spec` profile. It starts the app, lets SpringDoc introspect the controllers, writes
`ProductCatalogAPI/target/openapi.yaml`, then stops the app — no manual `spring-boot:run` needed. It overrides the
datasource to `localhost:5433` for the duration of the run (the default `application.yaml` URL points at the `postgres`
hostname used *inside* the docker-compose network, which isn't resolvable from the host). You just need Postgres
reachable on `localhost:5433` matching the compose credentials:

```bash
docker compose -f ProductCatalogDocker/src/main/resources/docker/docker-compose.yml up -d postgres

cd ProductCatalogAPI
./mvnw -P generate-openapi-spec verify
# output: target/openapi.yaml
```

## API Overview

| Method | Path                     | Description                                      |
|--------|--------------------------|---------------------------------------------------|
| GET    | `/api/v1/products`       | List/filter by `category`, `minPrice`, `maxPrice`, `name`, `active`; paged via `page`/`size` |
| GET    | `/api/v1/products/feed`  | Same filters, keyset-paginated via `afterId`/`limit` |
| GET    | `/api/v1/products/{id}`  | Fetch a single product                            |
| POST   | `/api/v1/products`       | Create a product                                  |
| PUT    | `/api/v1/products/{id}`  | Update a product                                  |
| DELETE | `/api/v1/products/{id}`  | Delete a product                                  |

## Pending work

- Response-level caching (e.g. Redis or HTTP caching headers) for hot category pages
- Read replica routing for the query side, since reads vastly outnumber writes
- CI/CD pipeline and OpenShift deployment automation