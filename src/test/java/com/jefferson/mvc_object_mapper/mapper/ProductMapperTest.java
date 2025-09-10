package com.jefferson.mvc_object_mapper.mapper;

import com.jefferson.mvc_object_mapper.dto.ProductDto;
import com.jefferson.mvc_object_mapper.dto.ProductRequest;
import com.jefferson.mvc_object_mapper.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = ProductMapperImpl.class)
public class ProductMapperTest {

    @Autowired
    private ProductMapper productMapper;

    @Test
    void toDto_shouldMapSuccessfully() {
        Long productId = 1L;
        String name = "test name";
        String description = "test description";
        BigDecimal price = BigDecimal.valueOf(12.50);
        Long quantityInStock = 5L;

        Product product = Product.build(name, description, price, quantityInStock);
        product.setId(productId);

        ProductDto expected = new ProductDto(productId, name, description, price,
                quantityInStock, LocalDateTime.now());

        ProductDto actual = productMapper.toDto(product);

        assertEquals(expected, actual);
    }

    @Test
    void toDto_shouldMapToDtoWithDefaultDescription() {
        Long productId = 1L;
        String name = "test name";
        BigDecimal price = BigDecimal.valueOf(12.50);
        Long quantityInStock = 5L;

        Product product = Product.build(name, null, price, quantityInStock);
        product.setId(productId);

        ProductDto expected = new ProductDto(productId, name, "", price,
                quantityInStock, LocalDateTime.now());

        ProductDto actual = productMapper.toDto(product);

        assertEquals(expected, actual);
    }

    @Test
    void toEntity_ShouldMapSuccessfully() {
        String name = "test name";
        String description = "test description";
        BigDecimal price = BigDecimal.valueOf(12.50);
        Long quantityInStock = 5L;

        ProductRequest productRequest = new ProductRequest(name, description, price, quantityInStock);

        Product expected = Product.build(name, description, price, quantityInStock);

        Product actual = productMapper.toEntity(productRequest);

        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getPrice(), actual.getPrice());
        assertEquals(expected.getQuantityInStock(), actual.getQuantityInStock());
    }

    @Test
    void toEntity_ShouldMapWithDefaultDescription() {
        String name = "test name";
        BigDecimal price = BigDecimal.valueOf(12.50);
        Long quantityInStock = 5L;

        ProductRequest productRequest = new ProductRequest(name, null, price, quantityInStock);

        Product expected = Product.build(name, "", price, quantityInStock);

        Product actual = productMapper.toEntity(productRequest);

        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getPrice(), actual.getPrice());
        assertEquals(expected.getQuantityInStock(), actual.getQuantityInStock());
    }
}
