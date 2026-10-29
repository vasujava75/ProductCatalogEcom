package com.api.catalog.productcatalogapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DatabaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
        assertThat(jdbcTemplate).isNotNull();
    }

    @Test
    void databaseConnectionWorks() {
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        assertThat(result).isEqualTo(1);
    }

    @Test
    void flywayMigrationsApplied() {
        Integer productsTableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'products'",
                Integer.class
        );
        assertThat(productsTableCount).isEqualTo(1);
    }

    @Test
    void categoryAndPriceIndexesExist() {
        Integer indexCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pg_indexes WHERE tablename = 'products' AND indexname = 'idx_products_category_price'",
                Integer.class
        );
        assertThat(indexCount).isEqualTo(1);
    }
}
