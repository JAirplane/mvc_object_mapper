package com.jefferson.mvc_object_mapper.mapper;

import com.jefferson.mvc_object_mapper.common.OrderStatus;
import com.jefferson.mvc_object_mapper.dto.OrderDto;
import com.jefferson.mvc_object_mapper.dto.OrderRequest;
import com.jefferson.mvc_object_mapper.dto.ProductDto;
import com.jefferson.mvc_object_mapper.model.Customer;
import com.jefferson.mvc_object_mapper.model.Order;
import com.jefferson.mvc_object_mapper.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = OrderMapperImpl.class)
public class OrderMapperTest {

    @Autowired
    private OrderMapper orderMapper;

    private Customer testCustomer;
    private Product testProduct1;
    private Product testProduct2;
    private Order testOrder;
    private ProductDto productDto1;
    private ProductDto productDto2;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setFirstName("Test Customer First Name");
        testCustomer.setLastName("Test Customer Last Name");
        testCustomer.setEmail("test@email.com");
        testCustomer.setDeleted(false);

        testProduct1 = new Product();
        testProduct1.setId(1L);
        testProduct1.setName("Product 1");
        testProduct1.setDescription("description 1");
        testProduct1.setPrice(BigDecimal.valueOf(100));
        testProduct1.setQuantityInStock(10L);
        testProduct1.setDeleted(false);

        testProduct2 = new Product();
        testProduct2.setId(2L);
        testProduct2.setName("Product 2");
        testProduct2.setDescription("description 2");
        testProduct2.setPrice(BigDecimal.valueOf(200));
        testProduct2.setQuantityInStock(20L);
        testProduct2.setDeleted(false);

        productDto1 = new ProductDto(
                1L,
                "Product 1",
                "description 1",
                BigDecimal.valueOf(100),
                10L,
                LocalDateTime.now()
        );

        productDto2 = new ProductDto(
                2L,
                "Product 2",
                "description 2",
                BigDecimal.valueOf(200),
                20L,
                LocalDateTime.now()
        );

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setCustomer(testCustomer);
        testOrder.addProduct(testProduct1);
        testOrder.addProduct(testProduct2);
        testOrder.setShippingAddress("Test Address 123");
        testOrder.setTotalPrice(BigDecimal.valueOf(300));
        testOrder.setOrderStatus(OrderStatus.PROCESSING);

        ReflectionTestUtils.setField(testOrder, "orderDate", LocalDateTime.now());
    }

    @Test
    void toDtoWithProducts_ShouldMapAllFieldsCorrectly() {

        OrderDto result = orderMapper.toDtoWithProducts(testOrder);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(testOrder.getId());
        assertThat(result.customerId()).isEqualTo(testOrder.getCustomer().getId());
        assertThat(result.shippingAddress()).isEqualTo(testOrder.getShippingAddress());
        assertThat(result.totalPrice()).isEqualTo(testOrder.getTotalPrice());
        assertThat(result.orderStatus()).isEqualTo(testOrder.getOrderStatus());
        assertThat(result.orderDate()).isEqualTo(testOrder.getOrderDate());

        assertThat(result.products()).hasSize(2);

        ProductDto resultProduct1 = result.products().get(0);
        assertThat(resultProduct1.id()).isEqualTo(testProduct1.getId());
        assertThat(resultProduct1.name()).isEqualTo(testProduct1.getName());
        assertThat(resultProduct1.description()).isEqualTo(testProduct1.getDescription());
        assertThat(resultProduct1.price()).isEqualTo(testProduct1.getPrice());
        assertThat(resultProduct1.quantityInStock()).isEqualTo(testProduct1.getQuantityInStock());

        ProductDto resultProduct2 = result.products().get(1);
        assertThat(resultProduct2.id()).isEqualTo(testProduct2.getId());
        assertThat(resultProduct2.name()).isEqualTo(testProduct2.getName());
        assertThat(resultProduct2.description()).isEqualTo(testProduct2.getDescription());
        assertThat(resultProduct2.price()).isEqualTo(testProduct2.getPrice());
        assertThat(resultProduct2.quantityInStock()).isEqualTo(testProduct2.getQuantityInStock());
    }

    @Test
    void toDtoWithoutProducts_ShouldMapWithoutProducts() {

        OrderDto result = orderMapper.toDtoWithoutProducts(testOrder);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(testOrder.getId());
        assertThat(result.customerId()).isEqualTo(testOrder.getCustomer().getId());
        assertThat(result.shippingAddress()).isEqualTo(testOrder.getShippingAddress());
        assertThat(result.totalPrice()).isEqualTo(testOrder.getTotalPrice());
        assertThat(result.orderStatus()).isEqualTo(testOrder.getOrderStatus());
        assertThat(result.orderDate()).isEqualTo(testOrder.getOrderDate());

        assertThat(result.products()).isNull();
    }

    @Test
    void toEntity_ShouldMapBasicFieldsAndIgnoreRelations() {

        OrderRequest request = new OrderRequest(
                1L,
                List.of(productDto1, productDto2),
                "Test Address",
                BigDecimal.valueOf(300)
        );

        Order result = orderMapper.toEntity(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getCustomer()).isNull();
        assertThat(result.getProducts()).isEmpty();
        assertThat(result.getShippingAddress()).isEqualTo(request.shippingAddress());
        assertThat(result.getTotalPrice()).isEqualTo(request.totalPrice());

        assertThat(result.getOrderDate()).isNull();
        assertThat(result.getOrderStatus()).isNull();
    }
}
