package com.codingshuttle.razorpay.payment.processor.dto.request;

import com.codingshuttle.razorpay.common.entity.Money;
import com.codingshuttle.razorpay.common.enums.PaymentMethod;

import java.util.Map;
import java.util.UUID;

public record PaymentProcessorRequest(
        UUID processingId,
        UUID paymentId,
        PaymentMethod paymentMethod,
        Money amount, //null for non card related methods
        String pan,//null for non card related methods
        String expiry,
        Map<String,Object> methodDetails


) {

    public static PaymentProcessorRequest card(UUID paymentId,String pan,String expiry, Money amount, Map<String,Object> methodDetails){
        return new PaymentProcessorRequest(UUID.randomUUID(),paymentId,PaymentMethod.CARD,amount,pan,expiry,methodDetails);

    }
    public static PaymentProcessorRequest nonCard(UUID paymentId,PaymentMethod paymentMethod ,Money amount, Map<String,Object> methodDetails){
        return new PaymentProcessorRequest(UUID.randomUUID(),paymentId,paymentMethod,amount,null,null,methodDetails);
    }

}
