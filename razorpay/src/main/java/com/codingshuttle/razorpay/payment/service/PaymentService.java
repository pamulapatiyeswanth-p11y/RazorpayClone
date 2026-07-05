package com.codingshuttle.razorpay.payment.service;

import com.codingshuttle.razorpay.payment.dto.request.PaymentInitRequest;
import com.codingshuttle.razorpay.payment.dto.response.PaymentResponse;

import java.util.UUID;

public interface PaymentService {

    PaymentResponse initiatePayment(UUID merchantId, PaymentInitRequest request);
    PaymentResponse capture(UUID merchantId, UUID paymentId);


}
