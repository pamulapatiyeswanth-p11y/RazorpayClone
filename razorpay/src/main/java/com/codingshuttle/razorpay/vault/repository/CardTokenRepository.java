package com.codingshuttle.razorpay.vault.repository;

import com.codingshuttle.razorpay.vault.entity.CardToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CardTokenRepository extends JpaRepository<CardToken, Long> {
    Optional<CardToken> findByTokenAndRevokedAtIsNull(String cardToken);
}
