package com.codingshuttle.razorpay.vault.service;

import com.codingshuttle.razorpay.common.entity.Money;
import com.codingshuttle.razorpay.payment.processor.dto.response.PaymentProcessorResponse;
import com.codingshuttle.razorpay.vault.dto.request.TokenizeRequest;
import com.codingshuttle.razorpay.vault.dto.response.TokenizeResponse;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;


public interface VaultService {
    TokenizeResponse tokenizeCard(UUID merchantId, TokenizeRequest request);
    PaymentProcessorResponse charge(String token,UUID paymentId ,Money amount, Map<String,Object> methodDetails);

}
