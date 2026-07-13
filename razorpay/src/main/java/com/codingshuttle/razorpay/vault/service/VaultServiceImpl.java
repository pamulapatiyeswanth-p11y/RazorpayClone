package com.codingshuttle.razorpay.vault.service;

import com.codingshuttle.razorpay.common.entity.Money;
import com.codingshuttle.razorpay.common.enums.CardBrand;
import com.codingshuttle.razorpay.common.exception.ResourceNotFoundException;
import com.codingshuttle.razorpay.common.util.RandomizerUtil;
import com.codingshuttle.razorpay.payment.processor.PaymentProcessorRouter;
import com.codingshuttle.razorpay.payment.processor.dto.request.PaymentProcessorRequest;
import com.codingshuttle.razorpay.payment.processor.dto.response.PaymentProcessorResponse;
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
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VaultServiceImpl implements VaultService {

    private final VaultRepository vaultRepository;
    private final CardTokenRepository cardTokenRepository;
    private final BytesEncryptor dekEncryptor;
    private final PaymentProcessorRouter processorRouter;

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

    @Override
    public PaymentProcessorResponse charge(String token,UUID paymentId , Money amount, Map<String, Object> methodDetails) {
        CardToken cardToken = cardTokenRepository.findByTokenAndRevokedAtIsNull(token)
                .orElseThrow(() -> new ResourceNotFoundException("CARD_TOKEN", token));
        VaultCard vaultCard = cardToken.getVaultCard();
        byte[] panBytes = null;
        try {
            byte[] dek = dekEncryptor.decrypt(cardToken.getVaultCard().getEncryptedDek());
            panBytes = VaultEncryptionConfig.encryptor(dek).decrypt(vaultCard.getEncryptedPan());
            String pan = new String(panBytes, StandardCharsets.UTF_8);
            String expiry = vaultCard.getExpirationMonth() + "/" + vaultCard.getExpirationYear();
            log.info("Vault charge registered, token = {}***** ", token.substring(0, 4));
            PaymentProcessorRequest processorRequest = PaymentProcessorRequest.
                    card(paymentId, pan, expiry, amount, methodDetails);
            PaymentProcessorResponse response = processorRouter.charge(processorRequest);

            return response;
        }
        catch (Exception e){
            return new PaymentProcessorResponse.Failure("VAULT_CHARGE_FAILED",e.getMessage());
        }
        finally {
            if (panBytes != null) {
                Arrays.fill(panBytes, (byte) 0); // Empty the pan bytes for security reasons.
            }
        }

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
