package com.codingshuttle.razorpay.payment.repository;

import com.codingshuttle.razorpay.payment.entity.PaymentTransitionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentTransitionLogRepository extends JpaRepository<PaymentTransitionLog, Long> {
}
