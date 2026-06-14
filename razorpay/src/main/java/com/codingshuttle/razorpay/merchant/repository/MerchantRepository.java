package com.codingshuttle.razorpay.merchant.repository;

import com.codingshuttle.razorpay.merchant.entity.Merchant;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, UUID> {
    Boolean existsByEmail(String email);

}
