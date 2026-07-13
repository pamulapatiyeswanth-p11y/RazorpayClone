package com.codingshuttle.razorpay.merchant.dto.request;

public record LoginRequest(
        String email,
        String password
) {
}
