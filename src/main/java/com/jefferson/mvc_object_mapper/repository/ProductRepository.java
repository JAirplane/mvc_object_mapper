package com.jefferson.mvc_object_mapper.repository;

import com.jefferson.mvc_object_mapper.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface ProductRepository extends CrudRepository<Product, Long>,
        PagingAndSortingRepository<Product, Long> {

    Page<Product> findAllByDeletedFalse(Pageable pageable);
    Optional<Product> findByIdAndDeletedFalse(Long id);
}
