package com.codingshuttle.razorpay.payment.dto.request;

import com.codingshuttle.razorpay.common.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record PaymentInitRequest(
        @NotNull(message = "Order id is required")
        UUID orderId,

        @NotNull(message = "Paymeny method is required")
        PaymentMethod paymentMethod,

        Map<String,Object> methodDetails

) {
}
