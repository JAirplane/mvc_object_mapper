package com.jefferson.mvc_object_mapper.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Objects;

public record CustomerDto(
        @NotNull(message = "Customer dto: id mustn't be null")
        @Positive(message = "Customer dto: id must be positive")
        Long id,
        @NotBlank(message = "Customer dto: first name mustn't be empty")
        String firstName,
        @NotBlank(message = "Customer dto: last name mustn't be empty")
        String lastName,
        @NotBlank(message = "Customer dto: email mustn't be empty")
        @Email(message = "Customer dto: email must be valid")
        String email,
        //Number validation in PhoneNumber class
        @NotBlank(message = "Customer dto: phoneNumber mustn't be empty")
        String phoneNumber) {

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;

        CustomerDto other = (CustomerDto) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(firstName, other.firstName)
                && Objects.equals(lastName, other.lastName)
                && Objects.equals(email, other.email)
                && Objects.equals(phoneNumber, other.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, email, phoneNumber);
    }
}
