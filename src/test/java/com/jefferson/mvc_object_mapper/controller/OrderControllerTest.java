package com.jefferson.mvc_object_mapper.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jefferson.mvc_object_mapper.common.OrderStatus;
import com.jefferson.mvc_object_mapper.dto.OrderDto;
import com.jefferson.mvc_object_mapper.dto.OrderRequest;
import com.jefferson.mvc_object_mapper.dto.ProductDto;
import com.jefferson.mvc_object_mapper.exception.CustomerNotFoundException;
import com.jefferson.mvc_object_mapper.exception.OrderNotFoundException;
import com.jefferson.mvc_object_mapper.service.OrderService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    private final Long validOrderId = 1L;
    private final Long validCustomerId = 1L;
    private OrderDto testOrderDto;
    private OrderRequest testOrderRequest;
    private List<ProductDto> testProducts;

    @BeforeEach
    void setUp() {
        testProducts = List.of(
                new ProductDto(1L, "Product 1", "Description 1",
                        new BigDecimal("19.99"), 10L, LocalDateTime.now()),
                new ProductDto(2L, "Product 2", "Description 2",
                        new BigDecimal("29.99"), 5L, LocalDateTime.now())
        );

        testOrderDto = new OrderDto(
                validOrderId,
                validCustomerId,
                testProducts,
                LocalDateTime.of(2024, 1, 15, 10, 30),
                "123 Main St, City, Country",
                new BigDecimal("49.98"),
                OrderStatus.PROCESSING
        );

        testOrderRequest = new OrderRequest(
                validCustomerId,
                testProducts,
                "123 Main St, City, Country",
                new BigDecimal("49.98")
        );
    }

    @Test
    void getOrder_ShouldReturnOrder_WhenOrderExists() throws Exception {

        when(orderService.getOrderById(validOrderId)).thenReturn(testOrderDto);

        mockMvc.perform(get("/api/v1/order/{id}", validOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(validOrderId))
                .andExpect(jsonPath("$.customerId").value(validCustomerId))
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(2))
                .andExpect(jsonPath("$.products[0].id").value(1))
                .andExpect(jsonPath("$.products[0].name").value("Product 1"))
                .andExpect(jsonPath("$.products[0].description").value("Description 1"))
                .andExpect(jsonPath("$.products[0].price").value(19.99))
                .andExpect(jsonPath("$.products[0].quantityInStock").value(10))
                .andExpect(jsonPath("$.products[0].createdAt").exists())
                .andExpect(jsonPath("$.shippingAddress").value("123 Main St, City, Country"))
                .andExpect(jsonPath("$.totalPrice").value(49.98))
                .andExpect(jsonPath("$.orderStatus").value("PROCESSING"));

        verify(orderService).getOrderById(validOrderId);
    }

    @Test
    void getOrder_ShouldReturnNotFound_WhenOrderNotExists() throws Exception {

        when(orderService.getOrderById(validOrderId))
                .thenThrow(new OrderNotFoundException("Order not found"));

        mockMvc.perform(get("/api/v1/order/{id}", validOrderId))
                .andExpect(status().isNotFound());

        verify(orderService).getOrderById(validOrderId);
    }

    @Test
    void newOrder_ShouldReturnCreatedOrder_WhenValidRequest() throws Exception {

        when(orderService.createNewOrder(any(OrderRequest.class))).thenReturn(testOrderDto);
        String requestBody = objectMapper.writeValueAsString(testOrderRequest);

        mockMvc.perform(post("/api/v1/order/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(validOrderId))
                .andExpect(jsonPath("$.customerId").value(validCustomerId))
                .andExpect(jsonPath("$.products.length()").value(2))
                .andExpect(jsonPath("$.products[0].id").value(1))
                .andExpect(jsonPath("$.products[0].name").value("Product 1"))
                .andExpect(jsonPath("$.products[0].quantityInStock").value(10))
                .andExpect(jsonPath("$.shippingAddress").value("123 Main St, City, Country"))
                .andExpect(jsonPath("$.totalPrice").value(49.98))
                .andExpect(jsonPath("$.orderStatus").value("PROCESSING"));

        verify(orderService).createNewOrder(any(OrderRequest.class));
    }

    @Test
    void newOrder_ShouldReturnNotFound_WhenCustomerNotFound() throws Exception {

        when(orderService.createNewOrder(any(OrderRequest.class)))
                .thenThrow(new CustomerNotFoundException("Customer not found"));
        String requestBody = objectMapper.writeValueAsString(testOrderRequest);

        mockMvc.perform(post("/api/v1/order/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());

        verify(orderService).createNewOrder(any(OrderRequest.class));
    }

    @Test
    void newOrder_ShouldReturnBadRequest_WhenValidationFails() throws Exception {

        when(orderService.createNewOrder(any(OrderRequest.class)))
                .thenThrow(new ConstraintViolationException("Validation failed", Set.of()));
        String requestBody = objectMapper.writeValueAsString(testOrderRequest);

        mockMvc.perform(post("/api/v1/order/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(orderService).createNewOrder(any(OrderRequest.class));
    }

    @Test
    void newOrder_ShouldReturnInternalServerError_WhenInvalidJson() throws Exception {

        String invalidJson = "{ invalid json }";

        mockMvc.perform(post("/api/v1/order/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isInternalServerError());

        verifyNoInteractions(orderService);
    }

    @Test
    void deleteOrder_ShouldReturnNoContent_WhenValidId() throws Exception {

        doNothing().when(orderService).softDeleteOrderById(validOrderId);

        mockMvc.perform(delete("/api/v1/order/{id}", validOrderId))
                .andExpect(status().isNoContent());

        verify(orderService).softDeleteOrderById(validOrderId);
    }

    @Test
    void deleteOrder_ShouldReturnBadRequest_WhenInvalidId() throws Exception {

        doThrow(new ConstraintViolationException("Invalid ID", Set.of()))
                .when(orderService).softDeleteOrderById(-1L);

        mockMvc.perform(delete("/api/v1/order/{id}", -1L))
                .andExpect(status().isBadRequest());

        verify(orderService).softDeleteOrderById(-1L);
    }
}
