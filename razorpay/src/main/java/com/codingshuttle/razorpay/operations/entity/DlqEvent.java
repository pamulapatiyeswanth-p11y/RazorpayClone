package com.codingshuttle.razorpay.operations.entity;

import com.codingshuttle.razorpay.common.entity.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class DlqEvent extends BaseEntity {
    // This is just a log for DLQ. We have a separate service for DLQ in Kafka
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "Webhook_event_id",nullable = false)
    private WebHookEvent webHookEventId;

    @Column(nullable = false)
    private UUID merchantId;

    @Column(length = 1050)
    private String finalError;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String,Object> payload;

    private LocalDateTime movedAt; // Event pushed to DLQ(dead letter queue)

    private LocalDateTime replayedAt; // Even processed at

}
