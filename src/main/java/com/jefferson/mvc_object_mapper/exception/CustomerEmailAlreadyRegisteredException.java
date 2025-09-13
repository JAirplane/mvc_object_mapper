package com.jefferson.mvc_object_mapper.exception;

public class CustomerEmailAlreadyRegisteredException extends RuntimeException {
    public CustomerEmailAlreadyRegisteredException(String message) {
        super(message);
    }
}
