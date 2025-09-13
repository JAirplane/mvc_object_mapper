package com.jefferson.mvc_object_mapper.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jefferson.mvc_object_mapper.dto.ProductDto;
import com.jefferson.mvc_object_mapper.dto.ProductRequest;
import com.jefferson.mvc_object_mapper.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(path = "/api/v1/product", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductController {

    private final ProductService productService;
    private final ObjectMapper objectMapper;

    @Autowired
    public ProductController(ProductService productService, ObjectMapper objectMapper) {
        this.productService = productService;
        this.objectMapper =objectMapper;
    }

    @GetMapping(path = "/all")
    public ResponseEntity<String> productsPage(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               @RequestParam(defaultValue = "id") String sort) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(sort));

            Page<ProductDto> products = productService.getAllProducts(pageable);

            String responseBody = objectMapper.writeValueAsString(products);

            return ResponseEntity.ok(responseBody);

        } catch (JsonProcessingException e) {
            return ResponseEntity.internalServerError().body("Error processing request");
        }
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<String> getProduct(@PathVariable Long id) {

        ProductDto productDto = productService.getProductById(id);

        try {
            String responseBody = objectMapper.writeValueAsString(productDto);

            return ResponseEntity.ok(responseBody);

        } catch (JsonProcessingException e) {
            return ResponseEntity.internalServerError().body("Error processing request");
        }
    }

    @PostMapping(path = "/new")
    public ResponseEntity<String> newProduct(@RequestBody String productRequest) {

        try {
            ProductRequest mappedRequest = objectMapper.readValue(productRequest, ProductRequest.class);

            ProductDto productDto = productService.createNewProduct(mappedRequest);

            String responseBody = objectMapper.writeValueAsString(productDto);

            return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);

        } catch (JsonProcessingException e) {
            return ResponseEntity.internalServerError().body("Error processing request");
        }
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<String> updateProduct(@PathVariable Long id,
                                                @RequestBody String updateRequest) {

        try {
            ProductRequest mappedRequest = objectMapper.readValue(updateRequest, ProductRequest.class);

            ProductDto productDto = productService.updateProductInfo(id, mappedRequest);

            String responseBody = objectMapper.writeValueAsString(productDto);

            return ResponseEntity.ok(responseBody);

        } catch (JsonProcessingException e) {
            return ResponseEntity.internalServerError().body("Error processing request");
        }
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) {

        productService.softDeleteProductById(id);
    }
}
