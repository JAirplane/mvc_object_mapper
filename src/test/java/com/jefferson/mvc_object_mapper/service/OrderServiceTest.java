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
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationInterceptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    private OrderService orderService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private OrderMapper orderMapper;

    @BeforeEach
    void initTests() {

        orderService = new OrderService(orderRepository, customerRepository, productRepository, orderMapper);

        var validatorFactory = new LocalValidatorFactoryBean();
        validatorFactory.afterPropertiesSet();

        var validationInterceptor = new MethodValidationInterceptor(validatorFactory.getValidator());

        var proxyFactory = new ProxyFactory(orderService);

        proxyFactory.addAdvice(validationInterceptor);

        orderService = (OrderService) proxyFactory.getProxy();
    }

    @Test
    void getOrderById_shouldReturnOrderDto_whenOrderExistsAndNotDeleted() {

        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        Customer customer = new Customer();
        customer.setId(1L);
        order.setCustomer(customer);
        order.setOrderStatus(OrderStatus.PROCESSING);

        OrderDto expectedDto = new OrderDto(
                orderId,
                1L,
                List.of(),
                LocalDateTime.now(),
                "Test Address",
                BigDecimal.valueOf(100.0),
                OrderStatus.PROCESSING
        );

        when(orderRepository.findByIdAndOrderStatusNot(orderId, OrderStatus.DELETED))
                .thenReturn(Optional.of(order));
        when(orderMapper.toDtoWithProducts(order)).thenReturn(expectedDto);

        OrderDto result = orderService.getOrderById(orderId);

        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(orderRepository).findByIdAndOrderStatusNot(orderId, OrderStatus.DELETED);
        verify(orderMapper).toDtoWithProducts(order);
    }

    @Test
    void getOrderById_shouldThrowOrderNotFoundException_whenOrderNotFound() {

        Long orderId = 999L;
        when(orderRepository.findByIdAndOrderStatusNot(orderId, OrderStatus.DELETED))
                .thenReturn(Optional.empty());

        OrderNotFoundException exception = assertThrows(OrderNotFoundException.class,
                () -> orderService.getOrderById(orderId));

        assertEquals("Order not found for id: " + orderId, exception.getMessage());
        verify(orderRepository).findByIdAndOrderStatusNot(orderId, OrderStatus.DELETED);
        verify(orderMapper, never()).toDtoWithProducts(any());
    }

    @Test
    void getOrderById_shouldThrowConstraintViolationException_whenOrderIdIsNull() {
        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class,
                () -> orderService.getOrderById(null));

        assertTrue(exception.getMessage().contains("Order id mustn't be null"));
        verify(orderRepository, never()).findByIdAndOrderStatusNot(any(), any());
        verify(orderMapper, never()).toDtoWithProducts(any());
    }

    @Test
    void getOrderById_shouldThrowConstraintViolationException_whenOrderIdIsNotPositive() {
        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class,
                () -> orderService.getOrderById(0L));

        assertTrue(exception.getMessage().contains("Order id must be positive"));
        verify(orderRepository, never()).findByIdAndOrderStatusNot(any(), any());
        verify(orderMapper, never()).toDtoWithProducts(any());
    }

    @Test
    void createNewOrder_shouldCreateOrderSuccessfully_whenValidRequest() {

        Long customerId = 1L;
        List<ProductDto> productDtos = List.of(
                new ProductDto(1L, "Product1", "Description1",
                        BigDecimal.valueOf(50.0), 10L, null),
                new ProductDto(2L, "Product2", "Description2",
                        BigDecimal.valueOf(30.0), 5L, null)
        );

        OrderRequest orderRequest = new OrderRequest(
                customerId,
                productDtos,
                "Test Address",
                BigDecimal.valueOf(80.0)
        );

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setDeleted(false);

        List<Product> products = List.of(
                new Product(1L, "Product1", "Description1",
                        BigDecimal.valueOf(50.0), 10L, false, LocalDateTime.now()),
                new Product(2L, "Product2", "Description2",
                        BigDecimal.valueOf(30.0), 5L, false, LocalDateTime.now())
        );

        Order orderEntity = new Order();
        orderEntity.setId(1L);
        orderEntity.setCustomer(customer);
        orderEntity.setShippingAddress("Test Address");
        orderEntity.setTotalPrice(BigDecimal.valueOf(80.0));

        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setCustomer(customer);
        savedOrder.setShippingAddress("Test Address");
        savedOrder.setTotalPrice(BigDecimal.valueOf(80.0));
        savedOrder.setOrderStatus(OrderStatus.PROCESSING);
        products.forEach(savedOrder::addProduct);

        OrderDto expectedDto = new OrderDto(
                1L,
                customerId,
                productDtos,
                savedOrder.getOrderDate(),
                "Test Address",
                BigDecimal.valueOf(80.0),
                OrderStatus.PROCESSING
        );

        when(customerRepository.findByIdAndDeletedFalse(customerId)).thenReturn(Optional.of(customer));
        when(productRepository.findAllByIdAndDeletedFalse(List.of(1L, 2L))).thenReturn(products);
        when(orderMapper.toEntity(orderRequest)).thenReturn(orderEntity);
        when(orderRepository.save(orderEntity)).thenReturn(savedOrder);
        when(orderMapper.toDtoWithProducts(savedOrder)).thenReturn(expectedDto);

        OrderDto result = orderService.createNewOrder(orderRequest);

        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(customerRepository).findByIdAndDeletedFalse(customerId);
        verify(productRepository).findAllByIdAndDeletedFalse(List.of(1L, 2L));
        verify(orderMapper).toEntity(orderRequest);
        verify(orderRepository).save(orderEntity);
        verify(orderMapper).toDtoWithProducts(savedOrder);

        assertEquals(2, orderEntity.getProducts().size());
    }

    @Test
    void createNewOrder_shouldThrowCustomerNotFoundException_whenCustomerNotFound() {

        Long customerId = 999L;
        OrderRequest orderRequest = new OrderRequest(
                customerId,
                List.of(),
                "Test Address",
                BigDecimal.valueOf(0.0)
        );

        when(customerRepository.findByIdAndDeletedFalse(customerId)).thenReturn(Optional.empty());

        CustomerNotFoundException exception = assertThrows(CustomerNotFoundException.class,
                () -> orderService.createNewOrder(orderRequest));

        assertEquals("Customer not found for id: " + customerId, exception.getMessage());
        verify(customerRepository).findByIdAndDeletedFalse(customerId);
        verify(productRepository, never()).findAllByIdAndDeletedFalse(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createNewOrder_shouldThrowProductNotFoundException_whenProductNotFound() {

        Long customerId = 1L;
        List<ProductDto> productDtos = List.of(
                new ProductDto(1L, "Product1", "Description1",
                        BigDecimal.valueOf(50.0), 10L, null),
                new ProductDto(999L, "NonExistent", "Description",
                        BigDecimal.valueOf(30.0), 5L, null)
        );

        OrderRequest orderRequest = new OrderRequest(
                customerId,
                productDtos,
                "Test Address",
                BigDecimal.valueOf(80.0)
        );

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setDeleted(false);

        List<Product> foundProducts = List.of(
                new Product(1L, "Product1", "Description1",
                        BigDecimal.valueOf(50.0), 10L, false, LocalDateTime.now())
        );

        when(customerRepository.findByIdAndDeletedFalse(customerId)).thenReturn(Optional.of(customer));
        when(productRepository.findAllByIdAndDeletedFalse(List.of(1L, 999L))).thenReturn(foundProducts);

        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
                () -> orderService.createNewOrder(orderRequest));

        assertEquals("Order contains product that wasn't found in db", exception.getMessage());
        verify(customerRepository).findByIdAndDeletedFalse(customerId);
        verify(productRepository).findAllByIdAndDeletedFalse(List.of(1L, 999L));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createNewOrder_shouldThrowConstraintViolationException_whenOrderRequestIsNull() {

        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class,
                () -> orderService.createNewOrder(null));

        assertTrue(exception.getMessage().contains("Order request mustn't be null"));
        verify(customerRepository, never()).findByIdAndDeletedFalse(any());
        verify(productRepository, never()).findAllByIdAndDeletedFalse(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createNewOrder_shouldThrowConstraintViolationException_whenOrderRequestHasInvalidCustomerId() {

        OrderRequest invalidRequest = new OrderRequest(
                null,
                List.of(),
                "Test Address",
                BigDecimal.valueOf(0.0)
        );

        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class,
                () -> orderService.createNewOrder(invalidRequest));

        assertTrue(exception.getMessage().contains("customer id mustn't be null"));
        verify(customerRepository, never()).findByIdAndDeletedFalse(any());
        verify(productRepository, never()).findAllByIdAndDeletedFalse(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createNewOrder_shouldThrowConstraintViolationException_whenOrderRequestHasInvalidProducts() {

        OrderRequest invalidRequest = new OrderRequest(
                1L,
                null,
                "Test Address",
                BigDecimal.valueOf(0.0)
        );

        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class,
                () -> orderService.createNewOrder(invalidRequest));

        assertTrue(exception.getMessage().contains("products mustn't be null"));
        verify(customerRepository, never()).findByIdAndDeletedFalse(any());
        verify(productRepository, never()).findAllByIdAndDeletedFalse(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void softDeleteOrderById_shouldSoftDeleteOrder_whenOrderExistsAndNotDeleted() {

        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setOrderStatus(OrderStatus.PROCESSING);

        when(orderRepository.findByIdAndOrderStatusNot(orderId, OrderStatus.DELETED))
                .thenReturn(Optional.of(order));

        orderService.softDeleteOrderById(orderId);

        verify(orderRepository).findByIdAndOrderStatusNot(orderId, OrderStatus.DELETED);
        verify(orderRepository).save(order);
        assertEquals(OrderStatus.DELETED, order.getOrderStatus());
    }

    @Test
    void softDeleteOrderById_shouldNotThrowException_whenOrderNotFound() {

        Long orderId = 999L;
        when(orderRepository.findByIdAndOrderStatusNot(orderId, OrderStatus.DELETED))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(() -> orderService.softDeleteOrderById(orderId));

        verify(orderRepository).findByIdAndOrderStatusNot(orderId, OrderStatus.DELETED);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void softDeleteOrderById_shouldThrowConstraintViolationException_whenOrderIdIsNull() {

        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class,
                () -> orderService.softDeleteOrderById(null));

        assertTrue(exception.getMessage().contains("Order id mustn't be null"));
        verify(orderRepository, never()).findByIdAndOrderStatusNot(any(), any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void softDeleteOrderById_shouldThrowConstraintViolationException_whenOrderIdIsZero() {

        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class,
                () -> orderService.softDeleteOrderById(0L));

        assertTrue(exception.getMessage().contains("Order id must be positive"));
        verify(orderRepository, never()).findByIdAndOrderStatusNot(any(), any());
        verify(orderRepository, never()).save(any());
    }
}
