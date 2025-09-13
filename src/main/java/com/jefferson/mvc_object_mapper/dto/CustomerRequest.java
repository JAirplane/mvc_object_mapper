package com.jefferson.mvc_object_mapper.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

public record CustomerRequest(@NotBlank(message = "Customer request: first name mustn't be empty")
                              String firstName,
                              @NotBlank(message = "Customer request: last name mustn't be empty")
                              String lastName,
                              @NotBlank(message = "Customer request: email mustn't be empty")
                              @Email(message = "Customer request: email must be valid")
                              String email,
                              //Number validation in PhoneNumber class
                              @NotBlank(message = "Customer request: phoneNumber mustn't be empty")
                              String phoneNumber) {

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;

        CustomerRequest other = (CustomerRequest) obj;
        return Objects.equals(firstName, other.firstName)
                && Objects.equals(lastName, other.lastName)
                && Objects.equals(email, other.email)
                && Objects.equals(phoneNumber, other.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, email, phoneNumber);
    }
}
