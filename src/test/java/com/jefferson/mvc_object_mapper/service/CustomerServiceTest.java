package com.jefferson.mvc_object_mapper.service;

import com.jefferson.mvc_object_mapper.dto.CustomerDto;
import com.jefferson.mvc_object_mapper.dto.CustomerRequest;
import com.jefferson.mvc_object_mapper.exception.CustomerEmailAlreadyRegisteredException;
import com.jefferson.mvc_object_mapper.exception.CustomerNotFoundException;
import com.jefferson.mvc_object_mapper.exception.PhoneNumberIsNotValidException;
import com.jefferson.mvc_object_mapper.mapper.CustomerMapper;
import com.jefferson.mvc_object_mapper.model.Customer;
import com.jefferson.mvc_object_mapper.model.PhoneNumber;
import com.jefferson.mvc_object_mapper.repository.CustomerRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationInterceptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    private CustomerService customerService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    private Customer testCustomer;
    private CustomerDto testCustomerDto;
    private final Long validCustomerId = 1L;

    @BeforeEach
    void initTests() {

        customerService = new CustomerService(customerRepository, customerMapper);

        var validatorFactory = new LocalValidatorFactoryBean();
        validatorFactory.afterPropertiesSet();

        var validationInterceptor = new MethodValidationInterceptor(validatorFactory.getValidator());

        var proxyFactory = new ProxyFactory(customerService);

        proxyFactory.addAdvice(validationInterceptor);

        customerService = (CustomerService) proxyFactory.getProxy();

        testCustomer = new Customer();
        testCustomer.setId(validCustomerId);
        testCustomer.setFirstName("John");
        testCustomer.setLastName("Doe");
        testCustomer.setEmail("john.doe@example.com");
        testCustomer.setPhoneNumber(new PhoneNumber("+1234567890"));
        testCustomer.setDeleted(false);

        testCustomerDto = new CustomerDto(
                validCustomerId,
                "John",
                "Doe",
                "john.doe@example.com",
                "+1234567890"
        );
    }

    @Test
    void getCustomerById_ShouldReturnCustomerDto_WhenValidIdAndCustomerExists() {

        when(customerRepository.findByIdAndDeletedFalse(validCustomerId))
                .thenReturn(Optional.of(testCustomer));
        when(customerMapper.toDto(testCustomer)).thenReturn(testCustomerDto);

        CustomerDto result = customerService.getCustomerById(validCustomerId);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testCustomerDto);

        verify(customerRepository).findByIdAndDeletedFalse(validCustomerId);
        verify(customerMapper).toDto(testCustomer);
    }

    @Test
    void getCustomerById_ShouldThrowCustomerNotFoundException_WhenCustomerNotFound() {

        when(customerRepository.findByIdAndDeletedFalse(validCustomerId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getCustomerById(validCustomerId))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessage("Customer not found for id: " + validCustomerId);

        verify(customerRepository).findByIdAndDeletedFalse(validCustomerId);
        verifyNoInteractions(customerMapper);
    }

    @Test
    void getCustomerById_ShouldThrowConstraintViolationException_WhenIdIsNull() {

        Long nullId = null;

        assertThatThrownBy(() -> customerService.getCustomerById(nullId))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("customerId") &&
                                            v.getMessage().equals("Customer id mustn't be null")
                            );
                });

        verifyNoInteractions(customerRepository);
        verifyNoInteractions(customerMapper);
    }

    @Test
    void getCustomerById_ShouldThrowConstraintViolationException_WhenIdIsZero() {

        Long zeroId = 0L;

        assertThatThrownBy(() -> customerService.getCustomerById(zeroId))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("customerId") &&
                                            v.getMessage().equals("Customer id must be positive")
                            );
                });

        verifyNoInteractions(customerRepository);
        verifyNoInteractions(customerMapper);
    }

    @Test
    void createNewCustomer_ShouldReturnCustomerDto_WhenValidRequestAndEmailNotExists() {

        CustomerRequest validRequest = new CustomerRequest(
                "John",
                "Doe",
                "john.doe@example.com",
                "+1234567890"
        );

        when(customerRepository.findByEmailIgnoreCaseAndDeletedFalse(validRequest.email()))
                .thenReturn(Optional.empty());
        when(customerMapper.toEntity(validRequest)).thenReturn(testCustomer);
        when(customerRepository.save(testCustomer)).thenReturn(testCustomer);
        when(customerMapper.toDto(testCustomer)).thenReturn(testCustomerDto);

        CustomerDto result = customerService.createNewCustomer(validRequest);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testCustomerDto);

        verify(customerRepository).findByEmailIgnoreCaseAndDeletedFalse(validRequest.email());
        verify(customerMapper).toEntity(validRequest);
        verify(customerRepository).save(testCustomer);
        verify(customerMapper).toDto(testCustomer);
    }

    @Test
    void createNewCustomer_ShouldThrowCustomerEmailAlreadyRegisteredException_WhenEmailAlreadyExists() {

        CustomerRequest validRequest = new CustomerRequest(
                "John",
                "Doe",
                "john.doe@example.com",
                "+1234567890"
        );

        when(customerRepository.findByEmailIgnoreCaseAndDeletedFalse(validRequest.email()))
                .thenReturn(Optional.of(testCustomer));

        assertThatThrownBy(() -> customerService.createNewCustomer(validRequest))
                .isInstanceOf(CustomerEmailAlreadyRegisteredException.class)
                .hasMessage("Customer with email: " + validRequest.email() + " already registered");

        verify(customerRepository).findByEmailIgnoreCaseAndDeletedFalse(validRequest.email());
        verifyNoInteractions(customerMapper);
        verify(customerRepository, never()).save(any());
    }

    @Test
    void createNewCustomer_ShouldThrowConstraintViolationException_WhenRequestIsNull() {

        CustomerRequest nullRequest = null;

        assertThatThrownBy(() -> customerService.createNewCustomer(nullRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("customerRequest") &&
                                            v.getMessage().equals("Customer request mustn't be null")
                            );
                });

        verifyNoInteractions(customerRepository);
        verifyNoInteractions(customerMapper);
    }

    @Test
    void createNewCustomer_ShouldThrowConstraintViolationException_WhenFirstNameIsBlank() {

        CustomerRequest invalidRequest = new CustomerRequest(
                "",
                "Doe",
                "john.doe@example.com",
                "+1234567890"
        );

        assertThatThrownBy(() -> customerService.createNewCustomer(invalidRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("firstName") &&
                                            v.getMessage().equals("Customer request: first name mustn't be empty")
                            );
                });

        verifyNoInteractions(customerRepository);
        verifyNoInteractions(customerMapper);
    }

    @Test
    void createNewCustomer_ShouldThrowConstraintViolationException_WhenLastNameIsBlank() {

        CustomerRequest invalidRequest = new CustomerRequest(
                "John",
                "",
                "john.doe@example.com",
                "+1234567890"
        );

        assertThatThrownBy(() -> customerService.createNewCustomer(invalidRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("lastName") &&
                                            v.getMessage().equals("Customer request: last name mustn't be empty")
                            );
                });

        verifyNoInteractions(customerRepository);
        verifyNoInteractions(customerMapper);
    }

    @Test
    void createNewCustomer_ShouldThrowConstraintViolationException_WhenEmailIsBlank() {

        CustomerRequest invalidRequest = new CustomerRequest(
                "John",
                "Doe",
                "", // blank email
                "+1234567890"
        );

        assertThatThrownBy(() -> customerService.createNewCustomer(invalidRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("email") &&
                                            v.getMessage().equals("Customer request: email mustn't be empty")
                            );
                });

        verifyNoInteractions(customerRepository);
        verifyNoInteractions(customerMapper);
    }

    @Test
    void createNewCustomer_ShouldThrowConstraintViolationException_WhenEmailIsInvalid() {

        CustomerRequest invalidRequest = new CustomerRequest(
                "John",
                "Doe",
                "invalid-email",
                "+1234567890"
        );

        assertThatThrownBy(() -> customerService.createNewCustomer(invalidRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("email") &&
                                            v.getMessage().equals("Customer request: email must be valid")
                            );
                });

        verifyNoInteractions(customerRepository);
        verifyNoInteractions(customerMapper);
    }

    @Test
    void createNewCustomer_ShouldThrowConstraintViolationException_WhenPhoneNumberIsBlank() {

        CustomerRequest invalidRequest = new CustomerRequest(
                "John",
                "Doe",
                "john.doe@example.com",
                ""
        );

        assertThatThrownBy(() -> customerService.createNewCustomer(invalidRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("phoneNumber") &&
                                            v.getMessage().equals("Customer request: phoneNumber mustn't be empty")
                            );
                });

        verifyNoInteractions(customerRepository);
        verifyNoInteractions(customerMapper);
    }

    @Test
    void createNewCustomer_ShouldThrowPhoneNumberIsNotValidException_WhenPhoneNumberIsInvalid() {

        CustomerRequest invalidRequest = new CustomerRequest(
                "John",
                "Doe",
                "john.doe@example.com",
                "invalid-phone"
        );

        when(customerRepository.findByEmailIgnoreCaseAndDeletedFalse(invalidRequest.email()))
                .thenReturn(Optional.empty());
        when(customerMapper.toEntity(invalidRequest))
                .thenThrow(new PhoneNumberIsNotValidException("Invalid phone number"));

        assertThatThrownBy(() -> customerService.createNewCustomer(invalidRequest))
                .isInstanceOf(PhoneNumberIsNotValidException.class)
                .hasMessage("Invalid phone number");

        verify(customerRepository).findByEmailIgnoreCaseAndDeletedFalse(invalidRequest.email());
        verify(customerMapper).toEntity(invalidRequest);
        verify(customerRepository, never()).save(any());
    }

    @Test
    void deleteCustomerById_ShouldSoftDeleteCustomer_WhenCustomerExists() {

        when(customerRepository.findByIdAndDeletedFalse(validCustomerId))
                .thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(testCustomer)).thenReturn(testCustomer);

        customerService.deleteCustomerById(validCustomerId);

        assertThat(testCustomer.isDeleted()).isTrue();
        verify(customerRepository).findByIdAndDeletedFalse(validCustomerId);
        verify(customerRepository).save(testCustomer);
    }

    @Test
    void deleteCustomerById_ShouldThrowConstraintViolationException_WhenIdIsNull() {

        Long nullId = null;

        assertThatThrownBy(() -> customerService.deleteCustomerById(nullId))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("customerId") &&
                                            v.getMessage().equals("Customer id mustn't be null")
                            );
                });

        verifyNoInteractions(customerRepository);
    }

    @Test
    void deleteCustomerById_ShouldThrowConstraintViolationException_WhenIdIsZero() {

        Long zeroId = 0L;

        assertThatThrownBy(() -> customerService.deleteCustomerById(zeroId))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("customerId") &&
                                            v.getMessage().equals("Customer id must be positive")
                            );
                });

        verifyNoInteractions(customerRepository);
    }
}
