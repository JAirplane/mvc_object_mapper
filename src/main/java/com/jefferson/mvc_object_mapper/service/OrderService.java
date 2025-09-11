package com.jefferson.mvc_object_mapper.service;

import com.jefferson.mvc_object_mapper.common.OrderStatus;
import com.jefferson.mvc_object_mapper.dto.OrderDto;
import com.jefferson.mvc_object_mapper.dto.OrderRequest;
import com.jefferson.mvc_object_mapper.dto.ProductDto;
import com.jefferson.mvc_object_mapper.exception.CustomerNotFoundException;
import com.jefferson.mvc_object_mapper.exception.OrderNotFoundException;
import com.jefferson.mvc_object_mapper.exception.ProductNotFoundException;
import com.jefferson.mvc_object_mapper.mapper.OrderMapper;
import com.jefferson.mvc_object_mapper.model.Customer;
import com.jefferson.mvc_object_mapper.model.Order;
import com.jefferson.mvc_object_mapper.model.Product;
import com.jefferson.mvc_object_mapper.repository.CustomerRepository;
import com.jefferson.mvc_object_mapper.repository.OrderRepository;
import com.jefferson.mvc_object_mapper.repository.ProductRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@Service
@Validated
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    @Autowired
    public OrderService(OrderRepository orderRepository, CustomerRepository customerRepository,
                        ProductRepository productRepository, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.customerRepository =customerRepository;
        this.productRepository = productRepository;
        this.orderMapper = orderMapper;
    }

    public OrderDto getOrderById(@NotNull(message = "Order id mustn't be null")
                                 @Positive(message = "Order id must be positive")
                                 Long orderId) {

        Order order = orderRepository.findByIdAndOrderStatusNot(orderId, OrderStatus.DELETED)
                .orElseThrow(() -> new OrderNotFoundException("Order not found for id: " + orderId));

        return orderMapper.toDtoWithProducts(order);
    }

    @Transactional
    public OrderDto createNewOrder(@Valid
                          @NotNull(message = "Order request mustn't be null")
                          OrderRequest orderRequest) {

        Customer customer = customerRepository.findByIdAndDeletedFalse(orderRequest.customerId())
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found for id: " + orderRequest.customerId()));

        List<Long> productIds = orderRequest.products().stream()
                .map(ProductDto::id).toList();
        List<Product> products = productRepository.findAllByIdAndDeletedFalse(productIds);

        if(productIds.size() != products.size()) {
            throw new ProductNotFoundException("Order contains product that wasn't found in db");
        }

        Order order = orderMapper.toEntity(orderRequest);
        for(Product product: products) {
            order.addProduct(product);
        }
        order.setCustomer(customer);

        Order savedOrder = orderRepository.save(order);

        return orderMapper.toDtoWithProducts(savedOrder);
    }

    @Transactional
    public void softDeleteOrderById(@NotNull(message = "Order id mustn't be null")
                                      @Positive(message = "Order id must be positive")
                                      Long orderId) {

        Optional<Order> orderOptional = orderRepository.findByIdAndOrderStatusNot(orderId, OrderStatus.DELETED);

        if(orderOptional.isPresent()) {
            Order order = orderOptional.get();
            order.setOrderStatus(OrderStatus.DELETED);
            orderRepository.save(order);
        }
    }
}
