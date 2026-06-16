package com.codingshuttle.razorpay.payment.repository;

import com.codingshuttle.razorpay.payment.entity.Payments;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payments, UUID> {
    
}
