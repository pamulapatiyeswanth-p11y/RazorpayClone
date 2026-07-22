package com.codingshuttle.razorpay.payment.controller;

import com.codingshuttle.razorpay.merchant.security.MerchantContext;
import com.codingshuttle.razorpay.payment.dto.request.PaymentInitRequest;
import com.codingshuttle.razorpay.payment.dto.response.PaymentResponse;
import com.codingshuttle.razorpay.payment.service.PaymentService;
import com.codingshuttle.razorpay.payment.service.impl.PaymentServiceImplementation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("v1/payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final MerchantContext merchantContext;
//    UUID merchantId = UUID.fromString("dc74a1f1-d2c4-4a09-b37a-48be52e9b0f1");// Todo: replace it with merchant context
    @PostMapping
    public ResponseEntity<PaymentResponse> initiatePayment(@Valid @RequestBody PaymentInitRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.initiatePayment(merchantContext.getMerchantId(),request));
    }

    @PostMapping("/{paymentId}/capture")
    public ResponseEntity<PaymentResponse> capture(@PathVariable @NotNull UUID paymentId){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.capture(merchantContext.getMerchantId(),paymentId));
    }
}
