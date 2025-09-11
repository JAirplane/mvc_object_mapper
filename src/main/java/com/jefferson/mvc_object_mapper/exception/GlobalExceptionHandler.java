package com.jefferson.mvc_object_mapper.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.sqm.ParsingException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    //Controller validation exceptions handling
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>>
    handleValidationArgumentException(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        log.error("Validation errors found in Controller: {}", errors.size());
        errors.forEach((field, msg) -> log.error("Field: '{}': {}", field, msg));

        return ResponseEntity.badRequest().body(errors);
    }

    //Service validation exception handling
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>>
    handleConstraintViolationException(ConstraintViolationException exception) {
        Map<String, String> errors = new HashMap<>();
        exception.getConstraintViolations()
                .forEach(constraintViolation -> {
                    String path = constraintViolation.getPropertyPath().toString();
                    String[] paths = path.split("\\.");
                    errors.put(paths[paths.length - 1], constraintViolation.getMessage());
                });

        log.error("Validation errors found in Service: {}", errors.size());
        errors.forEach((field, msg) -> log.error("Field: '{}': {}", field, msg));

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(PhoneNumberIsNotValidException.class)
    public ResponseEntity<Map<String, String>> handlePhoneNumberException(PhoneNumberIsNotValidException exception) {
        log.warn(exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("Error", exception.getMessage()));
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleProductNotFoundException(ProductNotFoundException exception) {
        log.warn(exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("Error", exception.getMessage()));
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleOrderNotFoundException(OrderNotFoundException exception) {
        log.warn(exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("Error", exception.getMessage()));
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCustomerNotFoundException(CustomerNotFoundException exception) {
        log.warn(exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("Error", exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatchException(MethodArgumentTypeMismatchException exception) {
        log.error(exception.getMessage());
        return ResponseEntity.badRequest()
                .body(Map.of("Error", "Invalid format: " + exception.getValue()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        log.error(exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("Error", "Unique index or primary key violation."));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleBadRequestBody(HttpMessageNotReadableException exception) {
        log.error(exception.getMessage());
        return ResponseEntity
                .badRequest()
                .body(Map.of("Error", "Request body is null or cannot be read."));
    }

    @ExceptionHandler(ParsingException.class)
    public ResponseEntity<Map<String, String>> handleQueryParsingException(ParsingException exception) {
        log.error(exception.getMessage());
        return ResponseEntity
                .internalServerError()
                .body(Map.of("Error", "Bad query parsing."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleCommonException(Exception exception) {
        log.error(exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("Error", exception.getMessage()));
    }
}
