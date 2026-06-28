package com.codingshuttle.razorpay.operations.entity;

import com.codingshuttle.razorpay.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "settlement_payments")
public class SettlementPayment extends BaseEntity{
    @EmbeddedId
    private SettlementPaymentId settlementPaymentId;

    @MapsId("settlementId")
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "settlement_id", nullable = false)
    private Settlement settlement; // Column won't be created for this



}
