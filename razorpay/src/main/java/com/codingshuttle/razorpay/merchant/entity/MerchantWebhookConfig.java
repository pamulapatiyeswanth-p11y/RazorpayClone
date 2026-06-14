package com.codingshuttle.razorpay.merchant.entity;
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
@Table(name = "merchant_webhook_config")
public class MerchantWebhookConfig {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchantId;

    @Column(nullable = false, length = 200)
    private String targetUrl;

    @Column(length = 300)
    private String webhookSecretHash;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(length = 200)
    private String eventTypes;


}
