package com.codingshuttle.razorpay.merchant.entity;

import com.codingshuttle.razorpay.common.enums.BusinessType;
import com.codingshuttle.razorpay.common.enums.MerchantStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Builder
@Table(name = "merchants")
public class Merchant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false,length = 200)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 20)
    private String contactNumber;

    @Column(length = 100)
    private String businessName;

    @Column(length = 100)
    @Enumerated(EnumType.STRING)
    private BusinessType businessType;

    @Column(length = 250)
    private String businessDescription;

    @Column(length = 200)
    private String websiteUrl;

    @Column(length = 100,nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MerchantStatus status = MerchantStatus.PENDING_KYC;

    @Column(length = 20)
    private String gstId;

    @Column(length = 20)
    private String panId;

    @Column(length = 200)
    private String settlementBankAccount;

    @Column(length = 25)
    private String settlementBankIFSC;

    @Column(length = 100)
    private String settlementBankName;

    @Column(length = 100)
    private String settlementBankAccountHolderName;

    @Column(length = 100)
    private String settlementBankBranch;
//
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
//    private String createdBy;
//    private String updatedBy;
}
