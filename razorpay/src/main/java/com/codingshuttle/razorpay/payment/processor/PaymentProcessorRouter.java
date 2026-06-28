package com.codingshuttle.razorpay.payment.processor;

import com.codingshuttle.razorpay.common.enums.PaymentMethod;
import com.codingshuttle.razorpay.payment.mapper.PaymentMapper;
import com.codingshuttle.razorpay.payment.processor.dto.request.PaymentProcessorRequest;
import com.codingshuttle.razorpay.payment.processor.dto.response.PaymentProcessorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentProcessorRouter {
    private final Map<PaymentMethod,PaymentProcessor> paymentProcessors;
    public PaymentProcessorResponse charge(PaymentProcessorRequest request){
        PaymentProcessor paymentProcessor = paymentProcessors.get(request.paymentMethod());
        if(paymentProcessor == null){
            throw new IllegalArgumentException("Payment processor not found for the payment method: "+request.paymentMethod());
        }
       return paymentProcessor.charge(request);
    }


}
