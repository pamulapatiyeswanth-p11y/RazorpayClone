package com.codingshuttle.razorpay.merchant.services;


import com.codingshuttle.razorpay.merchant.dto.request.MerchantSignUpRequest;
import com.codingshuttle.razorpay.merchant.dto.response.MerchantResponse;

public interface AuthService {
    MerchantResponse signUp(MerchantSignUpRequest request);
}
