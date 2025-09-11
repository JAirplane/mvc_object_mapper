package com.jefferson.mvc_object_mapper.mapper;

import com.jefferson.mvc_object_mapper.dto.OrderDto;
import com.jefferson.mvc_object_mapper.dto.OrderRequest;
import com.jefferson.mvc_object_mapper.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "products", ignore = true)
    @Mapping(source = "customer.id", target = "customerId")
    OrderDto toDtoWithoutProducts(Order order);

    @Mapping(source = "customer.id", target = "customerId")
    OrderDto toDtoWithProducts(Order order);

    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "products", ignore = true)
    Order toEntity(OrderRequest orderRequest);
}
