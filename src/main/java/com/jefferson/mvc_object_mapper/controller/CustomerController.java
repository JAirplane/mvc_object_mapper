package com.jefferson.mvc_object_mapper.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jefferson.mvc_object_mapper.dto.CustomerDto;
import com.jefferson.mvc_object_mapper.dto.CustomerRequest;
import com.jefferson.mvc_object_mapper.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(path = "/api/v1/customer", produces = MediaType.APPLICATION_JSON_VALUE)
public class CustomerController {

    private final CustomerService customerService;
    private final ObjectMapper objectMapper;

    @Autowired
    public CustomerController(CustomerService customerService,
                              ObjectMapper objectMapper) {

        this.customerService = customerService;
        this.objectMapper =objectMapper;
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<String> getCustomer(@PathVariable Long id) {

        CustomerDto customerDto = customerService.getCustomerById(id);
        try {
            String result = objectMapper.writeValueAsString(customerDto);
            return ResponseEntity.ok(result);
        } catch (JsonProcessingException e) {
            return ResponseEntity.internalServerError().body("Error processing request");
        }
    }

    @PostMapping(path = "/new")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> createCustomer(@RequestBody String request) {

        try {
            CustomerRequest customerRequest = objectMapper.readValue(request, CustomerRequest.class);
            CustomerDto response = customerService.createNewCustomer(customerRequest);
            String responseBody = objectMapper.writeValueAsString(response);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
        } catch (JsonProcessingException e) {
            return ResponseEntity.internalServerError().body("Error processing request");
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomer(@PathVariable Long id) {

        customerService.deleteCustomerById(id);
    }
}
