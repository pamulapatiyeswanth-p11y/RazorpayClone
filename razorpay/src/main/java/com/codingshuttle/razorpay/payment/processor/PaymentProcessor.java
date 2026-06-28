package com.codingshuttle.razorpay.payment.processor;

import com.codingshuttle.razorpay.payment.processor.dto.request.PaymentProcessorRequest;
import com.codingshuttle.razorpay.payment.processor.dto.response.PaymentProcessorResponse;

public interface PaymentProcessor {

    PaymentProcessorResponse charge(PaymentProcessorRequest request);
}
