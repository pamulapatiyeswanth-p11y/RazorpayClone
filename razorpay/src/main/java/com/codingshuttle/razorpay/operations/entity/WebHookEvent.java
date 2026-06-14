package com.codingshuttle.razorpay.operations.entity;


import com.codingshuttle.razorpay.common.enums.WebHookEventStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "Webhook_events")
public class WebHookEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "merchant_id",nullable = false)
    private UUID merchantId;


    @Column(nullable = false,length = 20)
    private String event_type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String,Object> payload;

    @Column(nullable = false)
    private String webhookSecret;

    @Column(nullable = false, length = 200)
    private String targetUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private WebHookEventStatus status = WebHookEventStatus.PENDING;

    @Column(nullable = false)
    private Integer attempts = 0;

    private LocalDateTime nextRetryAt;

    private LocalDateTime lastRetryAt;

    private LocalDateTime deliveredAt;

    @Column(length = 10)
    private int lastResponseCode;
    @Column(length = 1050)
    private String lastResponseBody;


}
