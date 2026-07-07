package com.codingshuttle.razorpay.vault.service;

import com.codingshuttle.razorpay.vault.dto.request.TokenizeRequest;
import com.codingshuttle.razorpay.vault.dto.response.TokenizeResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;


public interface VaultService {
    TokenizeResponse tokenizeCard(UUID merchantId, TokenizeRequest request);

}
