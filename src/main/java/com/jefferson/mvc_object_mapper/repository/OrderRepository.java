package com.jefferson.mvc_object_mapper.repository;

import com.jefferson.mvc_object_mapper.common.OrderStatus;
import com.jefferson.mvc_object_mapper.model.Order;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface OrderRepository extends CrudRepository<Order, Long> {

    Optional<Order> findByIdAndOrderStatusNot(Long id, OrderStatus status);
}
