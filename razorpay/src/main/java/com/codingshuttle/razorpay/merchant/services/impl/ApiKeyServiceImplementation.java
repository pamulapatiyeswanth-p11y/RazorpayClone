package com.codingshuttle.razorpay.merchant.services.impl;

import com.codingshuttle.razorpay.common.exception.ResourceNotFoundException;
import com.codingshuttle.razorpay.common.util.RandomizerUtil;
import com.codingshuttle.razorpay.merchant.dto.request.CreateApiKeyRequest;
import com.codingshuttle.razorpay.merchant.dto.response.ApiKeyResponse;
import com.codingshuttle.razorpay.merchant.dto.response.CreateApiKeyResponse;
import com.codingshuttle.razorpay.merchant.entity.ApiKey;
import com.codingshuttle.razorpay.merchant.entity.Merchant;
import com.codingshuttle.razorpay.merchant.mapper.ApiKeyMapper;
import com.codingshuttle.razorpay.merchant.repository.ApiKeyRepository;
import com.codingshuttle.razorpay.merchant.repository.MerchantRepository;
import com.codingshuttle.razorpay.merchant.services.ApiKeyService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class ApiKeyServiceImplementation implements ApiKeyService {
    private final MerchantRepository merchantRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyMapper apiKeyMapper;
    @Override
    @Transactional
    public CreateApiKeyResponse create(UUID merchantId, CreateApiKeyRequest request) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant", merchantId));

        String keyId = "rzp_"+request.environment().name().toLowerCase()+"_"+ RandomizerUtil.randomBase64(24);
        log.info("KeyId: {}",keyId);
        String rawSecret = RandomizerUtil.randomBase64(40);// Todo: Encode the raw secret with BcryptPasswordEncoder
        log.info("Raw Secret: {}",rawSecret);
        ApiKey apiKey = ApiKey.builder()
                .keyId(keyId)
                .keySecretHash(rawSecret)
                .environment(request.environment())
                .merchantId(merchant)
                .build();
        apiKey = apiKeyRepository.save(apiKey);
        return apiKeyMapper.toCreateResponse(apiKey);
//        return new CreateApiKeyResponse(apiKey.getId(),keyId,rawSecret,request.environment());
    }

    @Override
    public List<ApiKeyResponse> listByMerchant(UUID merchantId) {
       List<ApiKey> apiKeyList =  apiKeyRepository.findByMerchantId_Id(merchantId);
       return apiKeyMapper.toApiKeyResponseList(apiKeyList);
//        return apiKeyList.stream().map(apiKey -> new ApiKeyResponse(apiKey.getId(),
//                apiKey.getKeyId(),
//                apiKey.getEnvironment(),
//                apiKey.isEnabled(),
//                apiKey.getLastUsedAt(),
//                null)).toList();
    }

    @Override
    @Transactional
    public void revoke(UUID merchantId, UUID keyId) {
        ApiKey apiKey = apiKeyRepository.findById(keyId)
                .filter(key -> key.getMerchantId().getId().equals(merchantId))
                .orElseThrow(()-> new ResourceNotFoundException("ApiKey",keyId));
        apiKey.setEnabled(false);
//        apiKeyRepository.save(apiKey);
    }

    @Override
    @Transactional
    public CreateApiKeyResponse rotateKey(UUID merchantId, UUID keyId) {
        ApiKey apiKey = apiKeyRepository.findByIdAndMerchantId_Id(keyId,merchantId)
                .orElseThrow(()-> new ResourceNotFoundException("ApiKey",keyId));
        if(!apiKey.isEnabled()){
            throw new RuntimeException("Cannot rotate a disabled key.");
        }

        String newRawSecret = RandomizerUtil.randomBase64(40);
        apiKey.setPreviousKeySecretHash(apiKey.getKeySecretHash());
        apiKey.setKeySecretHash(newRawSecret); // Todo: Encode the raw secret with BcryptPasswordEncoder
        apiKey.setRotatedAt(LocalDateTime.now());
        apiKey.setGracePeriodExpiryAt(LocalDateTime.now().plusHours(24));
        apiKey = apiKeyRepository.save(apiKey);

        return new CreateApiKeyResponse(apiKey.getId(),apiKey.getKeyId(),newRawSecret,apiKey.getEnvironment());
    }


}
