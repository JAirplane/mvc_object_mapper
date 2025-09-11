package com.jefferson.mvc_object_mapper.service;

import com.jefferson.mvc_object_mapper.dto.ProductDto;
import com.jefferson.mvc_object_mapper.dto.ProductRequest;
import com.jefferson.mvc_object_mapper.exception.ProductNotFoundException;
import com.jefferson.mvc_object_mapper.mapper.ProductMapper;
import com.jefferson.mvc_object_mapper.model.Product;
import com.jefferson.mvc_object_mapper.repository.ProductRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.data.domain.*;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationInterceptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @BeforeEach
    void initTests() {

        productService = new ProductService(productRepository, productMapper);

        var validatorFactory = new LocalValidatorFactoryBean();
        validatorFactory.afterPropertiesSet();

        var validationInterceptor = new MethodValidationInterceptor(validatorFactory.getValidator());

        var proxyFactory = new ProxyFactory(productService);

        proxyFactory.addAdvice(validationInterceptor);

        productService = (ProductService) proxyFactory.getProxy();
    }

    @Test
    void getAllProducts_ShouldReturnPageOfProductDtos_WhenProductsExist() {

        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));

        Product product1 = Product.build("Product 1", "Description 1",
                new BigDecimal("100.00"), 10L);
        product1.setId(1L);

        Product product2 = Product.build("Product 2", "Description 2",
                new BigDecimal("200.00"), 20L);
        product2.setId(2L);

        List<Product> products = List.of(product1, product2);
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

        ProductDto dto1 = new ProductDto(1L, "Product 1", "Description 1",
                new BigDecimal("100.00"), 10L, LocalDateTime.now());
        ProductDto dto2 = new ProductDto(2L, "Product 2", "Description 2",
                new BigDecimal("200.00"), 20L, LocalDateTime.now());

        when(productRepository.findAllByDeletedFalse(pageable)).thenReturn(productPage);
        when(productMapper.toDto(product1)).thenReturn(dto1);
        when(productMapper.toDto(product2)).thenReturn(dto2);

        Page<ProductDto> result = productService.getAllProducts(pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(0, result.getNumber());

        List<ProductDto> content = result.getContent();
        assertEquals(2, content.size());

        assertEquals(dto1, content.get(0));
        assertEquals(dto2, content.get(1));

        verify(productRepository, times(1)).findAllByDeletedFalse(pageable);
        verify(productMapper, times(1)).toDto(product1);
        verify(productMapper, times(1)).toDto(product2);
    }

    @Test
    void getAllProducts_ShouldThrowConstraintViolationException_NullArg() {

        assertThatThrownBy(() -> productService.getAllProducts(null))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("pageable") &&
                                            v.getMessage().equals("Pageable arg mustn't be null"));
                });

        verifyNoInteractions(productRepository);
        verifyNoInteractions(productMapper);
    }

    @Test
    void getProductById_ShouldReturnProductDto_WhenProductExists() {

        Long productId = 1L;
        Product product = Product.build("Test Product", "Description",
                new BigDecimal("100.00"), 10L);
        product.setId(productId);

        ProductDto expectedDto = new ProductDto(productId, "Test Product", "Description",
                new BigDecimal("100.00"), 10L, LocalDateTime.now());

        when(productRepository.findByIdAndDeletedFalse(productId)).thenReturn(Optional.of(product));
        when(productMapper.toDto(product)).thenReturn(expectedDto);

        ProductDto result = productService.getProductById(productId);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedDto);

        verify(productRepository, times(1)).findByIdAndDeletedFalse(productId);
        verify(productMapper, times(1)).toDto(product);
    }

    @Test
    void getProductById_ShouldThrowProductNotFoundException_WhenProductNotFound() {
        Long productId = 999L;

        when(productRepository.findByIdAndDeletedFalse(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(productId))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product not found for id: " + productId);

        verify(productRepository, times(1)).findByIdAndDeletedFalse(productId);
        verify(productMapper, never()).toDto(any());
    }

    @Test
    void getProductById_ShouldThrowConstraintViolationException_WhenIdIsNull() {
        Long nullId = null;

        assertThatThrownBy(() -> productService.getProductById(nullId))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("productId") &&
                                            v.getMessage().equals("Product id mustn't be null"));
                });

        verifyNoInteractions(productRepository);
        verifyNoInteractions(productMapper);
    }

    @Test
    void getProductById_ShouldThrowConstraintViolationException_WhenIdIsNegative() {

        Long negativeId = -1L;

        assertThatThrownBy(() -> productService.getProductById(negativeId))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("productId") &&
                                            v.getMessage().equals("Product id must be positive"));
                });

        verifyNoInteractions(productRepository);
        verifyNoInteractions(productMapper);
    }


    @Test
    void createNewProduct_ShouldCreateAndReturnProductDto_WhenRequestIsValid() {

        ProductRequest request = new ProductRequest(
                "Test Product",
                "Test Description",
                new BigDecimal("100.00"),
                10L
        );

        Product newProduct = Product.build("Test Product", "Test Description",
                new BigDecimal("100.00"), 10L);

        Product savedProduct = Product.build("Test Product", "Test Description",
                new BigDecimal("100.00"), 10L);
        savedProduct.setId(1L);

        ProductDto expectedDto = new ProductDto(1L, "Test Product", "Test Description",
                new BigDecimal("100.00"), 10L, LocalDateTime.now());

        when(productMapper.toEntity(request)).thenReturn(newProduct);
        when(productRepository.save(newProduct)).thenReturn(savedProduct);
        when(productMapper.toDto(savedProduct)).thenReturn(expectedDto);

        ProductDto result = productService.createNewProduct(request);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedDto);
        assertThat(result.id()).isEqualTo(1L);

        verify(productMapper, times(1)).toEntity(request);
        verify(productRepository, times(1)).save(newProduct);
        verify(productMapper, times(1)).toDto(savedProduct);
    }

    @Test
    void createNewProduct_ShouldThrowConstraintViolationException_WhenRequestIsNull() {

        ProductRequest nullRequest = null;

        assertThatThrownBy(() -> productService.createNewProduct(nullRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("productRequest") &&
                                            v.getMessage().equals("Product request mustn't be null"));
                });

        verifyNoInteractions(productMapper);
        verifyNoInteractions(productRepository);
    }

    @Test
    void createNewProduct_ShouldThrowConstraintViolationException_WhenNameIsBlank() {

        ProductRequest invalidRequest = new ProductRequest(
                "", // empty name - invalid
                "Description",
                new BigDecimal("100.00"),
                10L
        );

        assertThatThrownBy(() -> productService.createNewProduct(invalidRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getMessage().equals("Product request: product name is null or empty"));
                });

        verifyNoInteractions(productMapper);
        verifyNoInteractions(productRepository);
    }

    @Test
    void createNewProduct_ShouldThrowConstraintViolationException_WhenPriceIsNegative() {

        ProductRequest invalidRequest = new ProductRequest(
                "Test Product",
                "Description",
                new BigDecimal("-100.00"),
                10L
        );

        assertThatThrownBy(() -> productService.createNewProduct(invalidRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getMessage().equals("Product request: price must be positive"));
                });

        verifyNoInteractions(productMapper);
        verifyNoInteractions(productRepository);
    }

    @Test
    void createNewProduct_ShouldThrowConstraintViolationException_WhenQuantityIsNull() {

        ProductRequest invalidRequest = new ProductRequest(
                "Test Product",
                "Description",
                new BigDecimal("100.00"),
                null
        );

        assertThatThrownBy(() -> productService.createNewProduct(invalidRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getMessage().equals("Product request: quantity in stock mustn't be null"));
                });

        verifyNoInteractions(productMapper);
        verifyNoInteractions(productRepository);
    }

    @Test
    void createNewProduct_ShouldThrowConstraintViolationException_WhenQuantityIsNegative() {

        ProductRequest invalidRequest = new ProductRequest(
                "Test Product",
                "Description",
                new BigDecimal("100.00"),
                -5L
        );

        assertThatThrownBy(() -> productService.createNewProduct(invalidRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getMessage().equals("Product request: quantity in stock must be positive or zero"));
                });

        verifyNoInteractions(productMapper);
        verifyNoInteractions(productRepository);
    }

    @Test
    void updateProductInfo_ShouldUpdateAndReturnProductDto_WhenInputIsValid() {

        Long productId = 1L;
        ProductRequest request = new ProductRequest(
                "Updated Product",
                "Updated Description",
                new BigDecimal("200.00"),
                20L
        );

        Product existingProduct = Product.build("Old Product", "Old Description",
                new BigDecimal("100.00"), 10L);
        existingProduct.setId(productId);

        Product updatedProduct = Product.build("Updated Product", "Updated Description",
                new BigDecimal("200.00"), 20L);
        updatedProduct.setId(productId);

        ProductDto expectedDto = new ProductDto(productId, "Updated Product", "Updated Description",
                new BigDecimal("200.00"), 20L, LocalDateTime.now());

        when(productRepository.findByIdAndDeletedFalse(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(existingProduct)).thenReturn(updatedProduct);
        when(productMapper.toDto(updatedProduct)).thenReturn(expectedDto);

        ProductDto result = productService.updateProductInfo(productId, request);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedDto);

        verify(productRepository, times(1)).findByIdAndDeletedFalse(productId);
        verify(productRepository, times(1)).save(existingProduct);
        verify(productMapper, times(1)).toDto(updatedProduct);

        assertThat(existingProduct.getName()).isEqualTo("Updated Product");
        assertThat(existingProduct.getDescription()).isEqualTo("Updated Description");
        assertThat(existingProduct.getPrice()).isEqualTo(new BigDecimal("200.00"));
        assertThat(existingProduct.getQuantityInStock()).isEqualTo(20L);
    }

    @Test
    void updateProductInfo_ShouldThrowProductNotFoundException_WhenProductNotFound() {

        Long productId = 999L;
        ProductRequest request = new ProductRequest(
                "Updated Product",
                "Description",
                new BigDecimal("100.00"),
                10L
        );

        when(productRepository.findByIdAndDeletedFalse(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProductInfo(productId, request))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product not found for id: " + productId);

        verify(productRepository, times(1)).findByIdAndDeletedFalse(productId);
        verify(productRepository, never()).save(any());
        verify(productMapper, never()).toDto(any());
    }

    @Test
    void updateProductInfo_ShouldThrowConstraintViolationException_WhenProductIdIsInvalid() {

        ProductRequest validRequest = new ProductRequest(
                "Test Product",
                "Description",
                new BigDecimal("100.00"),
                10L
        );

        Object[][] testCases = {
                {null, "Product id mustn't be null"},
                {-1L, "Product id must be positive"},
                {0L, "Product id must be positive"}
        };

        for (Object[] testCase : testCases) {
            Long invalidProductId = (Long) testCase[0];
            String expectedMessage = (String) testCase[1];

            assertThatThrownBy(() -> productService.updateProductInfo(invalidProductId, validRequest))
                    .isInstanceOf(ConstraintViolationException.class)
                    .satisfies(exception -> {
                        var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                        assertThat(violations).hasSize(1);
                        assertThat(violations)
                                .anyMatch(v -> v.getMessage().equals(expectedMessage));
                    });

            verifyNoInteractions(productRepository);
            verifyNoInteractions(productMapper);
        }
    }

    @Test
    void updateProductInfo_ShouldThrowConstraintViolationException_WhenRequestIsNull() {

        Long productId = 1L;
        ProductRequest nullRequest = null;

        assertThatThrownBy(() -> productService.updateProductInfo(productId, nullRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v -> v.getMessage().equals("Product request mustn't be null"));
                });

        verifyNoInteractions(productRepository);
        verifyNoInteractions(productMapper);
    }

    @Test
    void updateProductInfo_ShouldThrowConstraintViolationException_WhenRequestHasMultipleViolations() {

        Long productId = 1L;
        ProductRequest invalidRequest = new ProductRequest(
                "",
                "Description",
                new BigDecimal("-50.00"),
                -5L
        );

        assertThatThrownBy(() -> productService.updateProductInfo(productId, invalidRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(3);
                    assertThat(violations)
                            .extracting("message")
                            .containsExactlyInAnyOrder(
                                    "Product request: product name is null or empty",
                                    "Product request: price must be positive",
                                    "Product request: quantity in stock must be positive or zero"
                            );
                });

        verifyNoInteractions(productRepository);
        verifyNoInteractions(productMapper);
    }

    @Test
    void updateProductInfo_ShouldThrowConstraintViolationException_WhenBothParametersAreInvalid() {

        Long invalidProductId = -1L;
        ProductRequest invalidRequest = new ProductRequest(
                "",
                "Description",
                new BigDecimal("100.00"),
                -5L
        );

        assertThatThrownBy(() -> productService.updateProductInfo(invalidProductId, invalidRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(3);
                    assertThat(violations)
                            .extracting("message")
                            .containsExactlyInAnyOrder(
                                    "Product id must be positive",
                                    "Product request: product name is null or empty",
                                    "Product request: quantity in stock must be positive or zero"
                            );
                });

        verifyNoInteractions(productRepository);
        verifyNoInteractions(productMapper);
    }

    @Test
    void softDeleteProductById_ShouldSetDeletedToTrue_WhenProductExists() {

        Long productId = 1L;
        Product product = Product.build("Test Product", "Description",
                new BigDecimal("100.00"), 10L);
        product.setId(productId);
        product.setDeleted(false);

        when(productRepository.findByIdAndDeletedFalse(productId)).thenReturn(Optional.of(product));

        productService.softDeleteProductById(productId);

        assertThat(product.isDeleted()).isTrue();
        verify(productRepository, times(1)).findByIdAndDeletedFalse(productId);
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void softDeleteProductById_ShouldDoNothing_WhenProductNotFound() {

        Long productId = 999L;

        when(productRepository.findByIdAndDeletedFalse(productId)).thenReturn(Optional.empty());

        productService.softDeleteProductById(productId);

        verify(productRepository, times(1)).findByIdAndDeletedFalse(productId);
        verify(productRepository, never()).save(any());
    }

    @Test
    void softDeleteProductById_ShouldThrowConstraintViolationException_WhenProductIdIsNull() {

        Long nullId = null;

        assertThatThrownBy(() -> productService.softDeleteProductById(nullId))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v -> v.getMessage().equals("Product id mustn't be null"));
                });

        verifyNoInteractions(productRepository);
    }

    @Test
    void softDeleteProductById_ShouldThrowConstraintViolationException_WhenProductIdIsInvalid() {

        Long invalidProductId = 0L;

        assertThatThrownBy(() -> productService.softDeleteProductById(invalidProductId))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v -> v.getMessage().equals("Product id must be positive"));
                });

        verifyNoInteractions(productRepository);
    }
}
