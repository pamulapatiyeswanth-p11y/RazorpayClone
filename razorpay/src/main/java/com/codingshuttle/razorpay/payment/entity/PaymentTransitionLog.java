package com.codingshuttle.razorpay.payment.entity;
import com.codingshuttle.razorpay.common.entity.BaseEntity;
import com.codingshuttle.razorpay.common.enums.Actor;
import com.codingshuttle.razorpay.common.enums.PaymentEvent;
import com.codingshuttle.razorpay.common.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "payment_transition_log",
indexes = {
        @Index(name="idx_payment_log_payment_id",columnList = "payments_id")
})
public class PaymentTransitionLog extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "payments_id", nullable = false)
    private Payments payment;

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
