package com.codingshuttle.razorpay.merchant.entity;

import com.codingshuttle.razorpay.common.entity.BaseEntity;
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
@Table(name = "customer",
        indexes ={
                @Index(name = "idx_customer_merchant_id",columnList = "merchant_id"),
                @Index(name = "idx_customer_email",columnList = "email")})
public class Customer extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchantId;
    @Column(nullable = false,length = 200)
    private String name;
    @Column(nullable = false,length = 200)
    private String email;
    @Column(length = 20)
    private String contactNumber;
    @Column(length = 20)
    private String gstId;

    private LocalDateTime deletedAt;


}
