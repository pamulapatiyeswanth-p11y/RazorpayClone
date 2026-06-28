package com.codingshuttle.razorpay.payment.processor.dto.request;

import com.codingshuttle.razorpay.common.entity.Money;
import com.codingshuttle.razorpay.common.enums.PaymentMethod;

import java.util.Map;

public record PaymentProcessorRequest(
        PaymentMethod paymentMethod,
        Money amount, //null for non card related methods
        String pan,//null for non card related methods
        String expiry,
        Map<String,Object> methodDetails


) {
}
