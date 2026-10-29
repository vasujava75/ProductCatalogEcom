package com.api.catalog.productcatalogapi.resource;

import com.api.catalog.productcatalogapi.TestcontainersConfiguration;
import com.api.catalog.productcatalogapi.dto.ProductDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class ProductCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private ProductDto createSampleProductDto(String sku) {
        ProductDto dto = new ProductDto();
        dto.setSku(sku);
        dto.setName("Test Product");
        dto.setDescription("A test product description");
        dto.setCategory("Electronics");
        dto.setPrice(new BigDecimal("29.99"));
        dto.setCurrency("USD");
        dto.setStockQuantity(100);
        dto.setActive(true);
        return dto;
    }

    @Test
    void shouldCreateProductSuccessfully() throws Exception {
        ProductDto productDto = createSampleProductDto("CTRL-SKU-1");

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.sku").value("CTRL-SKU-1"))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void shouldReturnConflictForDuplicateSku() throws Exception {
        ProductDto productDto = createSampleProductDto("CTRL-SKU-DUP");

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnBadRequestForInvalidProduct() throws Exception {
        ProductDto invalidProduct = new ProductDto(); // Missing required fields

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateExistingProduct() throws Exception {
        ProductDto originalProduct = createSampleProductDto("CTRL-SKU-2");
        String createResponse = mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(originalProduct)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ProductDto createdProduct = objectMapper.readValue(createResponse, ProductDto.class);
        Long productId = createdProduct.getId();

        ProductDto updatedProduct = createSampleProductDto("CTRL-SKU-2");
        updatedProduct.setName("Updated Product Name");
        updatedProduct.setPrice(new BigDecimal("39.99"));

        mockMvc.perform(put("/api/v1/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("Updated Product Name"))
                .andExpect(jsonPath("$.price").value(39.99));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentProduct() throws Exception {
        ProductDto productDto = createSampleProductDto("CTRL-SKU-3");
        Long nonExistentId = 999999L;

        mockMvc.perform(put("/api/v1/products/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteExistingProduct() throws Exception {
        ProductDto productDto = createSampleProductDto("CTRL-SKU-4");
        String createResponse = mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ProductDto createdProduct = objectMapper.readValue(createResponse, ProductDto.class);
        Long productId = createdProduct.getId();

        mockMvc.perform(delete("/api/v1/products/{id}", productId))
                .andExpect(status().isNoContent());

        mockMvc.perform(put("/api/v1/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentProduct() throws Exception {
        Long nonExistentId = 999999L;

        mockMvc.perform(delete("/api/v1/products/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldListAndFilterProductsWithPagination() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSampleProductDto("CTRL-SKU-5"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/products")
                        .param("category", "Electronics")
                        .param("minPrice", "10")
                        .param("maxPrice", "100")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.size").value(5));
    }
}
