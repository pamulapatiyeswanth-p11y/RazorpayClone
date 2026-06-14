package com.codingshuttle.razorpay.operations.entity;

import jakarta.persistence.*;
import lombok.Cleanup;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class DlqEvent {
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
