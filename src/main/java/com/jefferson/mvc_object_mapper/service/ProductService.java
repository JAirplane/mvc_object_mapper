package com.jefferson.mvc_object_mapper.service;

import com.jefferson.mvc_object_mapper.dto.ProductDto;
import com.jefferson.mvc_object_mapper.dto.ProductRequest;
import com.jefferson.mvc_object_mapper.exception.ProductNotFoundException;
import com.jefferson.mvc_object_mapper.mapper.ProductMapper;
import com.jefferson.mvc_object_mapper.model.Product;
import com.jefferson.mvc_object_mapper.repository.ProductRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@Service
@Validated
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Autowired
    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    public Page<ProductDto> getAllProducts(@NotNull(message = "Pageable arg mustn't be null")
                                           Pageable pageable) {

        return productRepository.findAllByDeletedFalse(pageable)
                .map(productMapper::toDto);
    }

    public ProductDto getProductById(@NotNull(message = "Product id mustn't be null")
                                     @Positive(message = "Product id must be positive")
                                     Long productId) {
        Product product = productRepository.findByIdAndDeletedFalse(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found for id: " + productId));

        return productMapper.toDto(product);
    }

    @Transactional
    public ProductDto createNewProduct(@Valid
                                       @NotNull(message = "Product request mustn't be null")
                                       ProductRequest productRequest) {

        Product newProduct = productMapper.toEntity(productRequest);

        Product savedProduct = productRepository.save(newProduct);

        return productMapper.toDto(savedProduct);
    }

    @Transactional
    public ProductDto updateProductInfo(@NotNull(message = "Product id mustn't be null")
                                        @Positive(message = "Product id must be positive")
                                        Long productId,
                                        @Valid
                                        @NotNull(message = "Product request mustn't be null")
                                        ProductRequest productRequest) {

        Product product = productRepository.findByIdAndDeletedFalse(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found for id: " + productId));

        product.setName(productRequest.name());
        product.setDescription(productRequest.description());
        product.setPrice(productRequest.price());
        product.setQuantityInStock(productRequest.quantityInStock());

        //it is more obvious
        productRepository.save(product);

        return productMapper.toDto(product);
    }

    @Transactional
    public void softDeleteProductById(@NotNull(message = "Product id mustn't be null")
                                      @Positive(message = "Product id must be positive")
                                      Long productId) {

        Optional<Product> productOptional = productRepository.findByIdAndDeletedFalse(productId);

        if(productOptional.isPresent()) {
            Product product = productOptional.get();
            product.setDeleted(true);
            productRepository.save(product);
        }
    }
}
