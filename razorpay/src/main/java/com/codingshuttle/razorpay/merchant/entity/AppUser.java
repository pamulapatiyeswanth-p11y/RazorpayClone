package com.codingshuttle.razorpay.merchant.entity;

import com.codingshuttle.razorpay.common.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "app_user")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchantId;

    @Column(nullable = false, length = 100,unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 35,nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;
}
