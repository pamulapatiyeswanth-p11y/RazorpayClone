package com.codingshuttle.razorpay.vault.customvalidator;

import jakarta.validation.ConstraintValidator;

import java.time.Year;

public class ExpiryYearValidation implements ConstraintValidator<ValidExpiryYear, Integer>
{

    @Override
    public boolean isValid(Integer year, jakarta.validation.ConstraintValidatorContext context) {
        if (year == null) {
            return false;
        }
        return year >= Year.now().getValue();
    }
}
