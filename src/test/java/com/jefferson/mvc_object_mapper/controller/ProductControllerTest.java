package com.jefferson.mvc_object_mapper.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jefferson.mvc_object_mapper.dto.ProductDto;
import com.jefferson.mvc_object_mapper.dto.ProductRequest;
import com.jefferson.mvc_object_mapper.exception.ProductNotFoundException;
import com.jefferson.mvc_object_mapper.service.ProductService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    private final Long validProductId = 1L;
    private ProductDto testProductDto;
    private ProductRequest testProductRequest;

    @BeforeEach
    void setUp() {
        testProductDto = new ProductDto(
                validProductId,
                "Test Product",
                "Test Description",
                new BigDecimal("99.99"),
                100L,
                LocalDateTime.of(2024, 1, 15, 10, 30)
        );

        testProductRequest = new ProductRequest(
                "Test Product",
                "Test Description",
                new BigDecimal("99.99"),
                100L
        );
    }

    @Test
    void getProduct_ShouldReturnProduct_WhenProductExists() throws Exception {

        when(productService.getProductById(validProductId)).thenReturn(testProductDto);

        mockMvc.perform(get("/api/v1/product/{id}", validProductId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(validProductId))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.price").value(99.99))
                .andExpect(jsonPath("$.quantityInStock").value(100))
                .andExpect(jsonPath("$.createdAt").exists());

        verify(productService).getProductById(validProductId);
    }

    @Test
    void getProduct_ShouldReturnNotFound_WhenProductNotExists() throws Exception {

        when(productService.getProductById(validProductId))
                .thenThrow(new ProductNotFoundException("Product not found"));

        mockMvc.perform(get("/api/v1/product/{id}", validProductId))
                .andExpect(status().isNotFound());

        verify(productService).getProductById(validProductId);
    }

    @Test
    void productsPage_ShouldReturnProductsPage_WhenValidRequest() throws Exception {

        Page<ProductDto> productsPage = new PageImpl<>(
                List.of(testProductDto),
                PageRequest.of(0, 10),
                1
        );

        when(productService.getAllProducts(any(Pageable.class))).thenReturn(productsPage);

        mockMvc.perform(get("/api/v1/product/all")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(validProductId))
                .andExpect(jsonPath("$.content[0].name").value("Test Product"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.number").value(0));

        verify(productService).getAllProducts(any(Pageable.class));
    }

    @Test
    void newProduct_ShouldReturnCreatedProduct_WhenValidRequest() throws Exception {

        when(productService.createNewProduct(any(ProductRequest.class))).thenReturn(testProductDto);
        String requestBody = objectMapper.writeValueAsString(testProductRequest);

        mockMvc.perform(post("/api/v1/product/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(validProductId))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.price").value(99.99))
                .andExpect(jsonPath("$.quantityInStock").value(100));

        verify(productService).createNewProduct(any(ProductRequest.class));
    }

    @Test
    void newProduct_ShouldReturnBadRequest_WhenValidationFails() throws Exception {

        when(productService.createNewProduct(any(ProductRequest.class)))
                .thenThrow(new ConstraintViolationException("Validation failed", Set.of()));
        String requestBody = objectMapper.writeValueAsString(testProductRequest);

        mockMvc.perform(post("/api/v1/product/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(productService).createNewProduct(any(ProductRequest.class));
    }

    @Test
    void newProduct_ShouldReturnInternalServerError_WhenInvalidJson() throws Exception {

        String invalidJson = "{ invalid json }";

        mockMvc.perform(post("/api/v1/product/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isInternalServerError());

        verifyNoInteractions(productService);
    }

    @Test
    void updateProduct_ShouldReturnUpdatedProduct_WhenValidRequest() throws Exception {

        when(productService.updateProductInfo(eq(validProductId), any(ProductRequest.class)))
                .thenReturn(testProductDto);
        String requestBody = objectMapper.writeValueAsString(testProductRequest);

        mockMvc.perform(put("/api/v1/product/{id}", validProductId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(validProductId))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.price").value(99.99))
                .andExpect(jsonPath("$.quantityInStock").value(100));

        verify(productService).updateProductInfo(eq(validProductId), any(ProductRequest.class));
    }

    @Test
    void updateProduct_ShouldReturnNotFound_WhenProductNotExists() throws Exception {

        when(productService.updateProductInfo(eq(validProductId), any(ProductRequest.class)))
                .thenThrow(new ProductNotFoundException("Product not found"));
        String requestBody = objectMapper.writeValueAsString(testProductRequest);

        mockMvc.perform(put("/api/v1/product/{id}", validProductId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());

        verify(productService).updateProductInfo(eq(validProductId), any(ProductRequest.class));
    }

    @Test
    void updateProduct_ShouldReturnBadRequest_WhenValidationFails() throws Exception {

        when(productService.updateProductInfo(eq(validProductId), any(ProductRequest.class)))
                .thenThrow(new ConstraintViolationException("Validation failed", Set.of()));
        String requestBody = objectMapper.writeValueAsString(testProductRequest);

        mockMvc.perform(put("/api/v1/product/{id}", validProductId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(productService).updateProductInfo(eq(validProductId), any(ProductRequest.class));
    }

    @Test
    void updateProduct_ShouldReturnInternalServerError_WhenInvalidJson() throws Exception {

        String invalidJson = "{ invalid json }";

        mockMvc.perform(put("/api/v1/product/{id}", validProductId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isInternalServerError());

        verifyNoInteractions(productService);
    }

    @Test
    void deleteProduct_ShouldReturnNoContent_WhenValidId() throws Exception {

        doNothing().when(productService).softDeleteProductById(validProductId);

        mockMvc.perform(delete("/api/v1/product/{id}", validProductId))
                .andExpect(status().isNoContent());

        verify(productService).softDeleteProductById(validProductId);
    }

    @Test
    void deleteProduct_ShouldReturnBadRequest_WhenInvalidId() throws Exception {

        doThrow(new ConstraintViolationException("Invalid ID", Set.of()))
                .when(productService).softDeleteProductById(-1L);

        mockMvc.perform(delete("/api/v1/product/{id}", -1L))
                .andExpect(status().isBadRequest());

        verify(productService).softDeleteProductById(-1L);
    }
}
