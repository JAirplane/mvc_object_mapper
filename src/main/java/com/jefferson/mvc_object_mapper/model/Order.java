package com.jefferson.mvc_object_mapper.model;

import com.jefferson.mvc_object_mapper.common.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    @Getter
    @Setter
    private Customer customer;

    @ManyToMany(
            fetch = FetchType.LAZY
    )
    @JoinTable(
            name = "order_products",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id"),
            uniqueConstraints = @UniqueConstraint(
                    columnNames = {"order_id", "product_id"},
                    name = "uc_order_product_unique"
            )
    )
    @Where(clause = "deleted = false")
    private List<Product> products = new ArrayList<>();

    @Column(name = "order_date", nullable = false, updatable = false)
    @Getter
    private LocalDateTime orderDate;

    @Column(name = "shipping_address", nullable = false)
    @Getter
    @Setter
    private String shippingAddress;

    @Column(name = "total_price", nullable = false)
    @Getter
    @Setter
    private BigDecimal totalPrice;

    @Column(name = "order_status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private OrderStatus orderStatus;

    @PrePersist
    protected void onCreate() {
        orderDate = LocalDateTime.now();
        orderStatus = OrderStatus.PROCESSING;
    }

    public void addProduct(Product product) {
        products.add(product);
    }

    public void removeProduct(Product product) {
        products.remove(product);
    }

    public List<Product> getProducts() {
        return List.copyOf(products);
    }

    @Override
    public boolean equals(Object a) {
        if(this == a) return true;
        if(a == null || getClass() != a.getClass()) return false;

        Order other = (Order) a;

        if(id == null && other.id == null) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hashCode(id) : getClass().hashCode();
    }

    public static Order build(Customer customer, String shippingAddress, BigDecimal totalPrice) {
        Order order = new Order();
        order.setCustomer(customer);
        order.setShippingAddress(shippingAddress);
        order.setTotalPrice(totalPrice);
        return order;
    }
}
