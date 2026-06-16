package com.codingshuttle.razorpay.merchant.services;

import com.codingshuttle.razorpay.merchant.dto.request.CreateApiKeyRequest;
import com.codingshuttle.razorpay.merchant.dto.response.ApiKeyResponse;
import com.codingshuttle.razorpay.merchant.dto.response.CreateApiKeyResponse;

import java.util.List;
import java.util.UUID;

public interface ApiKeyService {
    CreateApiKeyResponse create(UUID merchantId, CreateApiKeyRequest request);
    List<ApiKeyResponse> listByMerchant(UUID merchantId);
    void revoke(UUID merchantId, UUID keyId);
    CreateApiKeyResponse rotateKey(UUID merchantId,UUID keyId);
}
