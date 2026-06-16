package com.codingshuttle.razorpay.payment.dto.request;

import com.codingshuttle.razorpay.common.entity.Money;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record CreateOrderRequest(
        @NotNull(message = "Amount is required")
        Money amount,

        @Size(max = 100)
        String receipt,// Refers to the orderId coming from merchant which is optional depending on merchant

        Map<String, Object> notes,

        LocalDateTime expireAt

) {
}
