package com.codingshuttle.razorpay.payment.entity;
import com.codingshuttle.razorpay.common.enums.Actor;
import com.codingshuttle.razorpay.common.enums.PaymentEvent;
import com.codingshuttle.razorpay.common.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "payment_transition_log")
public class PaymentTransitionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payments paymentId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus fromStatus;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus toStatus;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentEvent eventType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Actor actor;

    @Column(length = 200)
    private String reason;

    @Column(nullable = false)
    private LocalDateTime occurredAt;
}
