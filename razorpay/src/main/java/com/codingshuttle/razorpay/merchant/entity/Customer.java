package com.codingshuttle.razorpay.merchant.entity;

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
@Table(name = "customer")
public class Customer {
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
