package com.codingshuttle.razorpay.payment.entity;

import com.codingshuttle.razorpay.common.entity.BaseEntity;
import com.codingshuttle.razorpay.common.entity.Money;
import com.codingshuttle.razorpay.common.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "order_record",
indexes = {
        @Index(name = "idx_order_record_id_merchant_id",columnList = "id,merchant_id"),
        @Index(name = "idx_order_record_merchant_id",columnList = "merchant_id")

})
public class OrderRecord extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    // Storing merchantId as UUID directly to avoid unnecessary joins
    // no fk : cross service reference to Merchant entity
    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Column(name = "customer_id", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus status = OrderStatus.CREATED;

    @Embedded
    private Money amount;

    @Column(length = 100)
    private String receipt;// Contains the orderId coming from merchant which is optional depending on merchant

    @Column(nullable = false)
    @Builder.Default
    private int attempts = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> notes;

    @Column(nullable = false)
    private LocalDateTime expireAt;

}
