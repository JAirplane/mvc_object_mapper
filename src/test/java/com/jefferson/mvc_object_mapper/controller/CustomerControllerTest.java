package com.jefferson.mvc_object_mapper.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jefferson.mvc_object_mapper.dto.CustomerDto;
import com.jefferson.mvc_object_mapper.dto.CustomerRequest;
import com.jefferson.mvc_object_mapper.exception.CustomerEmailAlreadyRegisteredException;
import com.jefferson.mvc_object_mapper.exception.CustomerNotFoundException;
import com.jefferson.mvc_object_mapper.exception.PhoneNumberIsNotValidException;
import com.jefferson.mvc_object_mapper.service.CustomerService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
public class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    private final Long validCustomerId = 1L;
    private CustomerDto testCustomerDto;
    private CustomerRequest testCustomerRequest;

    @BeforeEach
    void setUp() {
        testCustomerDto = new CustomerDto(
                validCustomerId,
                "John",
                "Doe",
                "john.doe@example.com",
                "+1234567890"
        );

        testCustomerRequest = new CustomerRequest(
                "John",
                "Doe",
                "john.doe@example.com",
                "+1234567890"
        );
    }

    @Test
    void getCustomer_ShouldReturnCustomer_WhenCustomerExists() throws Exception {

        when(customerService.getCustomerById(validCustomerId)).thenReturn(testCustomerDto);

        mockMvc.perform(get("/api/v1/customer/{id}", validCustomerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(validCustomerId))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("+1234567890"));

        verify(customerService).getCustomerById(validCustomerId);
    }

    @Test
    void getCustomer_ShouldReturnNotFound_WhenCustomerNotExists() throws Exception {

        when(customerService.getCustomerById(validCustomerId))
                .thenThrow(new CustomerNotFoundException("Customer not found"));

        mockMvc.perform(get("/api/v1/customer/{id}", validCustomerId))
                .andExpect(status().isNotFound());

        verify(customerService).getCustomerById(validCustomerId);
    }

    @Test
    void createCustomer_ShouldReturnCreatedCustomer_WhenValidRequest() throws Exception {

        when(customerService.createNewCustomer(any(CustomerRequest.class))).thenReturn(testCustomerDto);
        String requestBody = objectMapper.writeValueAsString(testCustomerRequest);

        mockMvc.perform(post("/api/v1/customer/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(validCustomerId))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("+1234567890"));

        verify(customerService).createNewCustomer(any(CustomerRequest.class));
    }

    @Test
    void createCustomer_ShouldReturnBadRequest_WhenEmailAlreadyExists() throws Exception {

        when(customerService.createNewCustomer(any(CustomerRequest.class)))
                .thenThrow(new CustomerEmailAlreadyRegisteredException("Email already registered"));
        String requestBody = objectMapper.writeValueAsString(testCustomerRequest);

        mockMvc.perform(post("/api/v1/customer/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(customerService).createNewCustomer(any(CustomerRequest.class));
    }

    @Test
    void createCustomer_ShouldReturnBadRequest_WhenInvalidPhoneNumber() throws Exception {

        when(customerService.createNewCustomer(any(CustomerRequest.class)))
                .thenThrow(new PhoneNumberIsNotValidException("Invalid phone number"));
        String requestBody = objectMapper.writeValueAsString(testCustomerRequest);

        mockMvc.perform(post("/api/v1/customer/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(customerService).createNewCustomer(any(CustomerRequest.class));
    }

    @Test
    void createCustomer_ShouldReturnInternalServerError_WhenInvalidJson() throws Exception {

        String invalidJson = "{ invalid json }";

        mockMvc.perform(post("/api/v1/customer/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isInternalServerError());

        verifyNoInteractions(customerService);
    }

    @Test
    void deleteCustomer_ShouldReturnNoContent_WhenValidId() throws Exception {

        doNothing().when(customerService).deleteCustomerById(validCustomerId);

        mockMvc.perform(delete("/api/v1/customer/{id}", validCustomerId))
                .andExpect(status().isNoContent());

        verify(customerService).deleteCustomerById(validCustomerId);
    }

    @Test
    void deleteCustomer_ShouldReturnBadRequest_WhenInvalidId() throws Exception {
        // Arrange
        doThrow(new ConstraintViolationException("Invalid ID", Set.of()))
                .when(customerService).deleteCustomerById(-1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/customer/{id}", -1L))
                .andExpect(status().isBadRequest());

        verify(customerService).deleteCustomerById(-1L);
    }
}
