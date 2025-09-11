package com.jefferson.mvc_object_mapper.dto;

import com.jefferson.mvc_object_mapper.common.OrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record OrderDto(
        @NotNull(message = "Order dto: id mustn't be null")
        @Positive(message = "Order dto: id must be positive")
        Long id,

        @NotNull(message = "Order dto: customer id mustn't be null")
        @Positive(message = "Order dto: customer id must be positive")
        Long customerId,

        @NotNull(message = "Order dto: products mustn't be null")
        List<ProductDto> products,

        @NotNull(message = "Order dto: products mustn't be null")
        LocalDateTime orderDate,

        @NotBlank(message = "Order dto: shipping address mustn't be empty")
        String shippingAddress,

        @NotNull(message = "Order dto: total price mustn't be null")
        @PositiveOrZero(message = "Order dto: total price must be positive or zero")
        BigDecimal totalPrice,

        @NotNull(message = "Order dto: order status mustn't be null")
        OrderStatus orderStatus
) {

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;

        OrderDto other = (OrderDto) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(customerId, other.customerId)
                && Objects.equals(products, other.products)
                && Objects.equals(shippingAddress, other.shippingAddress)
                && Objects.equals(totalPrice, other.totalPrice)
                && Objects.equals(orderStatus, other.orderStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, customerId, products, shippingAddress, totalPrice, orderStatus);
    }
}
