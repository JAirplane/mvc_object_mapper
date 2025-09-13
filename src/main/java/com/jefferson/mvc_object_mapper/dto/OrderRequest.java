package com.jefferson.mvc_object_mapper.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

public record OrderRequest(

        @NotNull(message = "Order request: customer id mustn't be null")
        @Positive(message = "Order request: customer id must be positive")
        Long customerId,

        @NotNull(message = "Order request: products mustn't be null")
        List<ProductDto> products,

        @NotBlank(message = "Order request: shipping address mustn't be empty")
        String shippingAddress,

        @NotNull(message = "Order request: total price mustn't be null")
        @PositiveOrZero(message = "Order request: total price must be positive or zero")
        BigDecimal totalPrice
) {
}
