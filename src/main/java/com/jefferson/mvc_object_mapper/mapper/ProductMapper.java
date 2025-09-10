package com.jefferson.mvc_object_mapper.mapper;

import com.jefferson.mvc_object_mapper.dto.ProductDto;
import com.jefferson.mvc_object_mapper.dto.ProductRequest;
import com.jefferson.mvc_object_mapper.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "description", defaultValue = "")
    ProductDto toDto(Product product);

    @Mapping(target = "description", defaultValue = "")
    Product toEntity(ProductRequest productRequest);
}
