package com.jefferson.mvc_object_mapper.service;

import com.jefferson.mvc_object_mapper.dto.CustomerDto;
import com.jefferson.mvc_object_mapper.dto.CustomerRequest;
import com.jefferson.mvc_object_mapper.exception.CustomerEmailAlreadyRegisteredException;
import com.jefferson.mvc_object_mapper.exception.CustomerNotFoundException;
import com.jefferson.mvc_object_mapper.mapper.CustomerMapper;
import com.jefferson.mvc_object_mapper.model.Customer;
import com.jefferson.mvc_object_mapper.repository.CustomerRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@Service
@Validated
public class CustomerService {

    private final CustomerRepository customerRepository;

    private final CustomerMapper customerMapper;

    @Autowired
    public CustomerService(CustomerRepository customerRepository,
                           CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    public CustomerDto getCustomerById(@NotNull(message = "Customer id mustn't be null")
                                 @Positive(message = "Customer id must be positive")
                                 Long customerId) {

        Customer customer = customerRepository.findByIdAndDeletedFalse(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found for id: " + customerId));

        return customerMapper.toDto(customer);
    }

    @Transactional
    public CustomerDto createNewCustomer(@Valid
                                         @NotNull(message = "Customer request mustn't be null")
                                         CustomerRequest customerRequest) {

        if(customerRepository.findByEmailIgnoreCaseAndDeletedFalse(customerRequest.email()).isPresent()) {
            throw new CustomerEmailAlreadyRegisteredException("Customer with email: " + customerRequest.email()
                                                                + " already registered");
        }

        Customer customer = customerMapper.toEntity(customerRequest);

        Customer savedCustomer = customerRepository.save(customer);

        return customerMapper.toDto(savedCustomer);
    }

    @Transactional
    public void deleteCustomerById(@NotNull(message = "Customer id mustn't be null")
                                   @Positive(message = "Customer id must be positive")
                                   Long customerId) {

        Optional<Customer> customerOptional = customerRepository.findByIdAndDeletedFalse(customerId);

        if(customerOptional.isPresent()) {
            Customer customer = customerOptional.get();
            customer.setDeleted(true);
            customerRepository.save(customer);
        }
    }
}
