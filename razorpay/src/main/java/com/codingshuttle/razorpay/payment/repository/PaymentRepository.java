package com.codingshuttle.razorpay.payment.repository;

import com.codingshuttle.razorpay.common.enums.PaymentStatus;
import com.codingshuttle.razorpay.payment.entity.OrderRecord;
import com.codingshuttle.razorpay.payment.entity.Payments;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payments, UUID> {

    List<Payments> findByOrder_Id(OrderRecord order);
    Optional<Payments> findByIdAndMerchantId(UUID id, UUID merchantId);
    List<Payments> findByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime globalWindow);
}
