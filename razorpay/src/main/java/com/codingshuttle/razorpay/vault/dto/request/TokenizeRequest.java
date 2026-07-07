package com.codingshuttle.razorpay.vault.dto.request;

import com.codingshuttle.razorpay.vault.customvalidator.ValidExpiryYear;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.LuhnCheck;

import java.time.LocalDate;
import java.time.Year;
import java.util.UUID;

public record TokenizeRequest(
        @NotBlank(message = "PAN is required")
        @LuhnCheck(message = "Invalid card number")// Luhn check for card number validation
        @Pattern(regexp = "^[0-9]{13,19}$", message = "Invalid card number")// Card number should be between 13 to 19 digits
        String pan,
        @NotBlank(message = "CVV is required")
        @Pattern(regexp = "^[0-9]{3,4}$", message ="Invalid CVV")// CVV should be 3 or 4 digits
        String cvv,

        UUID customerId,
        @Size(min = 3,max = 30,message = "Card holder name should have at least 3 characters and Maximum allowed length is 30")
        String cardHolderName,

        @NotBlank(message = "Expiry month is required")
        @Min(value = 1, message = "Expiry month should be between 1 and 12")
        @Max(value = 12, message = "Expiry month should be between 1 and 12")
        Integer expiryMonth,

        @NotBlank(message = "Expiry year is required")
        @ValidExpiryYear(message = "Expiry year should be current year or later")// Custom validator to check if expiry year is current year or later
        Integer expiryYear
       ) {
}
