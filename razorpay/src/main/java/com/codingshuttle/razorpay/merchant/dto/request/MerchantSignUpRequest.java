package com.codingshuttle.razorpay.merchant.dto.request;


import com.codingshuttle.razorpay.common.enums.BusinessType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

//@RequiredArgsConstructor
//@NoArgsConstructor
//@Builder
//@Data

public record MerchantSignUpRequest(
        @NotBlank(message = "Name should not be blank")
        @Size(max = 50, message = "Name should not be more than 50 characters long")
        String name,

        @NotBlank(message = "Email cannot be blank")
        @Email
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8,message = "Password should be at least 8 characters long")
        String password,

        @Size(max = 50,message = "Business name should not be more than 50 characters long")
        String businessName,

        BusinessType businessType
) {

}
