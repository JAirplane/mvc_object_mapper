package com.jefferson.mvc_object_mapper.mapper;

import com.jefferson.mvc_object_mapper.dto.CustomerDto;
import com.jefferson.mvc_object_mapper.dto.CustomerRequest;
import com.jefferson.mvc_object_mapper.exception.PhoneNumberIsNotValidException;
import com.jefferson.mvc_object_mapper.model.Customer;
import com.jefferson.mvc_object_mapper.model.PhoneNumber;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CustomerMapperImpl.class)
public class CustomerMapperTest {

    @Autowired
    private CustomerMapper customerMapper;

    @Test
    void testToDto_ShouldMapEntityToDtoCorrectly() {

        PhoneNumber phoneNumber = new PhoneNumber("+1234567890");
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john.doe@example.com");
        customer.setPhoneNumber(phoneNumber);
        customer.setDeleted(false);

        CustomerDto result = customerMapper.toDto(customer);

        assertNotNull(result);
        assertEquals(customer.getId(), result.id());
        assertEquals(customer.getFirstName(), result.firstName());
        assertEquals(customer.getLastName(), result.lastName());
        assertEquals(customer.getEmail(), result.email());
        assertEquals(customer.getPhoneNumber().getPhoneNumber(), result.phoneNumber());
    }

    @Test
    void testToEntity_ShouldMapRequestToEntityCorrectly() {

        CustomerRequest request = new CustomerRequest(
                "Jane",
                "Smith",
                "jane.smith@example.com",
                "+9876543210"
        );

        Customer result = customerMapper.toEntity(request);

        assertNotNull(result);
        assertNull(result.getId());
        assertEquals(request.firstName(), result.getFirstName());
        assertEquals(request.lastName(), result.getLastName());
        assertEquals(request.email(), result.getEmail());
        assertNotNull(result.getPhoneNumber());
        assertEquals(request.phoneNumber(), result.getPhoneNumber().getPhoneNumber());
        assertFalse(result.isDeleted());
        assertNull(result.getCreatedAt());
    }

    @Test
    void testToEntity_ShouldThrowPhoneNumberNotValid() {

        CustomerRequest request = new CustomerRequest(
                "Jane",
                "Smith",
                "jane.smith@example.com",
                "aaaaaaaa"
        );

        assertThatThrownBy(() -> customerMapper.toEntity(request))
                .isInstanceOf(PhoneNumberIsNotValidException.class)
                .hasMessage("Invalid phone number");
    }
}
