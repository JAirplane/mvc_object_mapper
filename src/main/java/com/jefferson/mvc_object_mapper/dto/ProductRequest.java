package com.jefferson.mvc_object_mapper.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ProductRequest(
         @NotBlank(message = "Product request: product name is null or empty")
         String name,

         String description,

         @Positive(message = "Product request: price must be positive")
         BigDecimal price,

         @NotNull(message = "Product request: quantity in stock mustn't be null")
         @PositiveOrZero(message = "Product request: quantity in stock must be positive or zero")
         Long quantityInStock) {
}
