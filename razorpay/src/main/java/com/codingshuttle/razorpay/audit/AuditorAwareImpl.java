package com.codingshuttle.razorpay.audit;


import com.codingshuttle.razorpay.merchant.security.MerchantContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditorAwareImpl")
@RequiredArgsConstructor
public class AuditorAwareImpl implements AuditorAware<String> {
    private final MerchantContext merchantContext;

    @Override
    public Optional<String> getCurrentAuditor() {
        try {
            String keyId = merchantContext.getKeyId();
            if (keyId != null && !keyId.isBlank()) {
                return Optional.of(keyId); // If key id present then it's the action from application
            }
            if (merchantContext.getMerchantId() != null) // If there is merchant id but no Key id then it's from razorpay dashboard.
            {
                return Optional.of("merchant_id: " + merchantContext.getMerchantId());
            }
        }catch (Exception ignored){

            // Do nothing
        }

        return Optional.of("SYSTEM");
    }
}
