package com.codingshuttle.razorpay.vault.dto.response;

import com.codingshuttle.razorpay.common.enums.CardBrand;
import com.codingshuttle.razorpay.common.enums.CardType;

public record TokenizeResponse(
        String token,
        CardBrand cardBrand,
//        CardType cardType,
        String lastFourDigits,
        String expiryMonth,
        String expiryYear
) {
}
