package com.jefferson.mvc_object_mapper.repository;

import com.jefferson.mvc_object_mapper.model.Customer;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CustomerRepository extends CrudRepository<Customer, Long> {

    Optional<Customer> findByIdAndDeletedFalse(Long id);
    Optional<Customer> findByEmailIgnoreCaseAndDeletedFalse(String email);
}
