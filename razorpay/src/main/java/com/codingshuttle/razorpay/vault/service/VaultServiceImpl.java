package com.codingshuttle.razorpay.vault.service;

import com.codingshuttle.razorpay.common.enums.CardBrand;
import com.codingshuttle.razorpay.common.util.RandomizerUtil;
import com.codingshuttle.razorpay.vault.config.VaultEncryptionConfig;
import com.codingshuttle.razorpay.vault.dto.request.TokenizeRequest;
import com.codingshuttle.razorpay.vault.dto.response.TokenizeResponse;
import com.codingshuttle.razorpay.vault.entity.CardToken;
import com.codingshuttle.razorpay.vault.entity.VaultCard;
import com.codingshuttle.razorpay.vault.repository.CardTokenRepository;
import com.codingshuttle.razorpay.vault.repository.VaultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VaultServiceImpl implements VaultService {

    private final VaultRepository vaultRepository;
    private final CardTokenRepository cardTokenRepository;
    private final BytesEncryptor dekEncryptor;

    @Override
    @Transactional
    public TokenizeResponse tokenizeCard(UUID merchantId, TokenizeRequest request) {
        String lastFour = request.pan().substring(request.pan().length()-4);
        String bin = request.pan().substring(0,6);
        CardBrand cardBrand = detectBrand(request.pan());
        byte[] dek = KeyGenerators.secureRandom(32).generateKey(); // Generate random key as dek to encrypt the pan
        byte[] encryptedPan = VaultEncryptionConfig.encryptor(dek).encrypt(request.pan().getBytes(StandardCharsets.UTF_8));
        byte[] encryptedDek = dekEncryptor.encrypt(dek);
        VaultCard vaultCard = vaultRepository.save(VaultCard.builder()
                .brand(cardBrand)
                .bin(bin)
                .expirationMonth(request.expiryMonth())
                .expirationYear(request.expiryYear())
                .lastFourDigits(lastFour)
                .encryptedDek(encryptedDek)
                .encryptedPan(encryptedPan)
                .cardholderName(request.cardHolderName())
                .build());
        String token = "tok_"+ RandomizerUtil.randomBase64(32);
        CardToken cardToken = cardTokenRepository.save(CardToken.builder()
                .vaultCard(vaultCard)
                .token(token)
                .customerId(request.customerId())
                .merchantId(merchantId)
                .build());


        return new TokenizeResponse(token,cardBrand,
                lastFour,
                request.expiryMonth().toString(),
                request.expiryYear().toString());
    }


    private CardBrand detectBrand(String pan){
        String num = pan.replaceAll("[\\s-]", "");

        if (num.startsWith("4") && (num.length() == 13 || num.length() == 16 || num.length() == 19)) {
            return CardBrand.VISA;
        }
        if (num.startsWith("5") || num.startsWith("2") && num.length() == 16) {
            return CardBrand.MASTERCARD;
        }
        if (num.startsWith("34") || num.startsWith("37")) {
            return CardBrand.AMEX;
        }
        if (num.startsWith("6011") && num.length() == 16) {
            return CardBrand.DISCOVER;
        }
        return CardBrand.RUPAY;
    }
}
