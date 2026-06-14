package com.codingshuttle.razorpay.operations.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "settlement_payments")
public class SettlementPayment {
    @EmbeddedId
    private SettlementPaymentId settlementPaymentId;

    @MapsId("settlementId")
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "settlement_id", nullable = false)
    private Settlement settlement; // Column won't be created for this



}
