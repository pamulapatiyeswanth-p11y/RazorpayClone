package com.codingshuttle.razorpay.merchant.repository;

import com.codingshuttle.razorpay.merchant.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
    List<ApiKey> findByMerchantId_Id(UUID merchantId);
    Optional<ApiKey> findByIdAndMerchantId_Id(UUID keyId, UUID merchantId);
    Optional<ApiKey> findByKeyId(String keyId);
}
