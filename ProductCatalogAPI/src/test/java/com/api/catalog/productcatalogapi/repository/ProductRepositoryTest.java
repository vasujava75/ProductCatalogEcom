package com.api.catalog.productcatalogapi.repository;

import com.api.catalog.productcatalogapi.TestcontainersConfiguration;
import com.api.catalog.productcatalogapi.domain.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    private Product electronicsA;
    private Product electronicsB;
    private Product apparel;

    @BeforeEach
    void setUp() {
        entityManager.getEntityManager().createQuery("DELETE FROM Product").executeUpdate();
        entityManager.flush();

        electronicsA = newProduct("SKU-A", "Wireless Mouse", "Electronics", "19.99", 500);
        electronicsB = newProduct("SKU-B", "Mechanical Keyboard", "Electronics", "79.99", 200);
        apparel = newProduct("SKU-C", "Denim Jacket", "Apparel", "59.99", 220);
    }

    private Product newProduct(String sku, String name, String category, String price, int stock) {
        Product product = new Product();
        product.setSku(sku);
        product.setName(name);
        product.setCategory(category);
        product.setPrice(new BigDecimal(price));
        product.setCurrency("USD");
        product.setStockQuantity(stock);
        product.setActive(true);
        return product;
    }

    @Test
    void shouldFindBySku() {
        entityManager.persistAndFlush(electronicsA);

        Optional<Product> found = productRepository.findBySku("SKU-A");

        assertTrue(found.isPresent());
        assertEquals("Wireless Mouse", found.get().getName());
    }

    @Test
    void shouldReportExistsBySku() {
        entityManager.persistAndFlush(electronicsA);

        assertTrue(productRepository.existsBySku("SKU-A"));
        assertFalse(productRepository.existsBySku("SKU-UNKNOWN"));
    }

    @Test
    void shouldFilterByCategoryUsingSpecification() {
        entityManager.persistAndFlush(electronicsA);
        entityManager.persistAndFlush(electronicsB);
        entityManager.persistAndFlush(apparel);

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        List<Product> electronics = productRepository.findAll(ProductSpecifications.hasCategory("Electronics"), pageable).getContent();

        assertEquals(2, electronics.size());
        assertTrue(electronics.stream().allMatch(p -> p.getCategory().equals("Electronics")));
    }

    @Test
    void shouldFilterByPriceRangeUsingSpecification() {
        entityManager.persistAndFlush(electronicsA);
        entityManager.persistAndFlush(electronicsB);
        entityManager.persistAndFlush(apparel);

        var spec = ProductSpecifications.priceGreaterThanOrEqual(new BigDecimal("50.00"))
                .and(ProductSpecifications.priceLessThanOrEqual(new BigDecimal("80.00")));

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        List<Product> inRange = productRepository.findAll(spec, pageable).getContent();

        assertEquals(2, inRange.size());
        assertTrue(inRange.stream().noneMatch(p -> p.getName().equals("Wireless Mouse")));
    }
}
