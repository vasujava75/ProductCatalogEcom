package com.api.catalog.productcatalogapi.service;

import com.api.catalog.productcatalogapi.domain.Product;
import com.api.catalog.productcatalogapi.dto.ProductDto;
import com.api.catalog.productcatalogapi.exceptions.DuplicateSkuException;
import com.api.catalog.productcatalogapi.exceptions.ProductNotFoundException;
import com.api.catalog.productcatalogapi.mapper.ProductMapper;
import com.api.catalog.productcatalogapi.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductCommandService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductValidationService validationService;

    public ProductCommandService(ProductRepository productRepository,
                                  ProductMapper productMapper,
                                  ProductValidationService validationService) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.validationService = validationService;
    }

    public ProductDto createProduct(ProductDto productDto) {
        if (productDto.getSku() != null && productRepository.existsBySku(productDto.getSku())) {
            throw new DuplicateSkuException("A product with SKU '" + productDto.getSku() + "' already exists");
        }

        Product product = productMapper.toEntity(productDto);
        validationService.validateProduct(product);
        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    public ProductDto updateProduct(Long id, ProductDto productDto) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        productMapper.updateEntity(existingProduct, productDto);
        validationService.validateProduct(existingProduct);
        Product savedProduct = productRepository.save(existingProduct);
        return productMapper.toDto(savedProduct);
    }

    public void deleteProduct(Long id) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        productRepository.delete(existingProduct);
    }
}
