package com.api.catalog.productcatalogapi;

import org.springframework.boot.SpringApplication;

public class TestProductCatalogApiApplication {

    public static void main(String[] args) {
        SpringApplication.from(ProductCatalogApiApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
