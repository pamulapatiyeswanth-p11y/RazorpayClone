package com.codingshuttle.razorpay.vault.repository;

import com.codingshuttle.razorpay.vault.entity.CardToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardTokenRepository extends JpaRepository<CardToken, Long> {
    CardToken findByCardToken(String cardToken);
}
