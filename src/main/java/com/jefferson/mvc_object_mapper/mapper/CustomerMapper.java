package com.jefferson.mvc_object_mapper.mapper;

import com.jefferson.mvc_object_mapper.dto.CustomerDto;
import com.jefferson.mvc_object_mapper.dto.CustomerRequest;
import com.jefferson.mvc_object_mapper.model.Customer;
import com.jefferson.mvc_object_mapper.model.PhoneNumber;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "phoneNumber", expression = "java(customer.getPhoneNumber().getPhoneNumber())")
    CustomerDto toDto(Customer customer);
    @Mapping(target = "phoneNumber", expression = "java(mapPhoneNumber(customerRequest.phoneNumber()))")
    Customer toEntity(CustomerRequest customerRequest);

    default PhoneNumber mapPhoneNumber(String phoneNumber) {
        return new PhoneNumber(phoneNumber);
    }
}
