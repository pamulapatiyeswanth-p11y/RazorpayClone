package com.codingshuttle.razorpay.merchant.cache;

import com.codingshuttle.razorpay.common.enums.Environment;
import com.codingshuttle.razorpay.merchant.entity.Merchant;
import jakarta.persistence.*;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApiKeyCacheEntry(

        UUID merchantId,
        String keyId,
        String keySecretHash,
        String previousKeySecretHash,
        Environment environment,
        boolean enabled,
        LocalDateTime gracePeriodExpiryAt

) {
    public boolean isInGracePeriod(){
        return gracePeriodExpiryAt !=null && LocalDateTime.now().isBefore(gracePeriodExpiryAt);
    }
}
