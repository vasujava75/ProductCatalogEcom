package com.api.catalog.productcatalogapi.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Product Catalog API",
                description = "A Spring Boot REST API for managing an e-commerce product catalog with filtering and pagination",
                version = "1.0.0",
                contact = @Contact(
                        name = "Product Catalog Team",
                        email = "support@productcatalog.com"
                ),
                license = @License(
                        name = "MIT",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local development server"),
                @Server(url = "https://product-catalog-api.apps.openshift.com", description = "Production server")
        }
)
public class OpenApiConfig {
}
