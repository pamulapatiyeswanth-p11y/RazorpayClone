package com.codingshuttle.razorpay.merchant.entity;
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
@Table(name = "merchant_webhook_config",
indexes = {
        @Index(name = "idx_webhook_merchant_id",columnList = "merchant_id,enabled")
})
public class MerchantWebhookConfig extends BaseEntity {
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
