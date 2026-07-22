package com.codingshuttle.razorpay.merchant.controller;

import com.codingshuttle.razorpay.merchant.dto.response.ApiKeyResponse;
import com.codingshuttle.razorpay.merchant.dto.request.CreateApiKeyRequest;
import com.codingshuttle.razorpay.merchant.dto.response.CreateApiKeyResponse;
import com.codingshuttle.razorpay.merchant.security.MerchantContext;
import com.codingshuttle.razorpay.merchant.services.impl.ApiKeyServiceImplementation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/merchants/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyServiceImplementation apiKeyService;
    private final MerchantContext merchantContext;

    @PostMapping("/generate-key")
    public ResponseEntity<CreateApiKeyResponse> create(
                                                       @Valid @RequestBody CreateApiKeyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(apiKeyService.create(merchantContext.getMerchantId(),request));
    }

    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> listByMerchant(){
        return ResponseEntity.ok().body(apiKeyService.listByMerchant(merchantContext.getMerchantId()));

    }


    @DeleteMapping("/{keyId}")
    public ResponseEntity<Void> revoke(@PathVariable UUID keyId){
        apiKeyService.revoke(merchantContext.getMerchantId(),keyId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{keyId}/rotate")
    public ResponseEntity<CreateApiKeyResponse> rotateKey(@PathVariable UUID keyId){
       return ResponseEntity.status(HttpStatus.CREATED).body( apiKeyService.rotateKey(merchantContext.getMerchantId(),keyId));
    }
}