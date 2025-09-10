package com.jefferson.mvc_object_mapper.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public record ProductDto(
        @NotNull(message = "Product dto: id mustn't be null")
        @Positive(message = "Product dto: id must be positive")
        Long id,

        @NotBlank(message = "Product dto: product name is null or empty")
        String name,

        //ProductMapper fills it with "" to avoid returning null
        String description,

        @Positive(message = "Product dto: price must be positive")
        BigDecimal price,

        @NotNull(message = "Product dto: quantity in stock mustn't be null")
        @PositiveOrZero(message = "Product dto: quantity in stock must be positive or zero")
        Long quantityInStock,

        LocalDateTime createdAt) {

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;

        ProductDto other = (ProductDto) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(name, other.name)
                && Objects.equals(description, other.description)
                && Objects.equals(price, other.price)
                && Objects.equals(quantityInStock, other.quantityInStock);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, price, quantityInStock);
    }
}
