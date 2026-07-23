package com.codingshuttle.razorpay.merchant.controller;


import com.codingshuttle.razorpay.merchant.cache.ApiKeyCacheEntry;
import com.codingshuttle.razorpay.merchant.dto.request.LoginRequest;
import com.codingshuttle.razorpay.merchant.dto.request.MerchantSignUpRequest;
import com.codingshuttle.razorpay.merchant.dto.response.LoginResponse;
import com.codingshuttle.razorpay.merchant.dto.response.MerchantResponse;
import com.codingshuttle.razorpay.merchant.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    @PostMapping("/signup")
    public ResponseEntity<MerchantResponse> singUp(
            @RequestBody @Valid MerchantSignUpRequest request)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signUp(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody @Valid LoginRequest request)
    {
        return ResponseEntity.status(HttpStatus.OK).body(authService.login(request));
    }


}
