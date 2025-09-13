package com.jefferson.mvc_object_mapper.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jefferson.mvc_object_mapper.dto.OrderDto;
import com.jefferson.mvc_object_mapper.dto.OrderRequest;
import com.jefferson.mvc_object_mapper.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(path = "/api/v1/order", produces = MediaType.APPLICATION_JSON_VALUE)
public class OrderController {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @Autowired
    public OrderController(OrderService orderService, ObjectMapper objectMapper) {
        this.orderService = orderService;
        this. objectMapper = objectMapper;
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<String> getOrder(@PathVariable Long id) {

        OrderDto orderDto = orderService.getOrderById(id);

        try {
            String responseBody = objectMapper.writeValueAsString(orderDto);
            return ResponseEntity.ok().body(responseBody);
        } catch (JsonProcessingException e) {
            return ResponseEntity.internalServerError().body("Error processing request");
        }
    }

    @PostMapping(path = "/new")
    public ResponseEntity<String> newOrder(@RequestBody String request) {

        try {
            OrderRequest orderRequest = objectMapper.readValue(request, OrderRequest.class);

            OrderDto orderDto = orderService.createNewOrder(orderRequest);

            String responseBody = objectMapper.writeValueAsString(orderDto);

            return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);

        } catch (JsonProcessingException e) {
            return ResponseEntity.internalServerError().body("Error processing request");
        }
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable Long id) {

        orderService.softDeleteOrderById(id);
    }
}
