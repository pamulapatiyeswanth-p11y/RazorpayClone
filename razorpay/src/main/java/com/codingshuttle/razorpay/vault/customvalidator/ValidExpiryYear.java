package com.codingshuttle.razorpay.vault.customvalidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD,ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented // Indicates that this annotation should be documented by javadoc and similar tools
@Constraint(validatedBy = ExpiryYearValidation.class)
public @interface ValidExpiryYear {
    String message() default "Expiry year must be greater than or equal to current year";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
