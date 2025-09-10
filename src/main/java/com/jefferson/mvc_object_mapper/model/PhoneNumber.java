package com.jefferson.mvc_object_mapper.model;

import com.jefferson.mvc_object_mapper.exception.PhoneNumberIsNotValidException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
public class PhoneNumber {

    @Column(name = "phone_number", length = 20, nullable = false)
    @Getter
    private String phoneNumber;

    public PhoneNumber(String phoneNumber) {
        setPhoneWithValidation(phoneNumber);
    }

    public void setPhoneNumber(String phoneNumber) {
        setPhoneWithValidation(phoneNumber);
    }

    private void setPhoneWithValidation(String phoneNumber) {
        if(isValid(phoneNumber)) {
            this.phoneNumber = phoneNumber.replaceAll("[^0-9+]", "");
        }
        else throw new PhoneNumberIsNotValidException("Invalid phone number");
    }

    public static boolean isValid(String phone) {
        return phone != null && phone.matches("^[+]?[0-9\\s\\-()]{5,20}$");
    }
}
