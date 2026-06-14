package com.codingshuttle.razorpay.vault.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "card_token")
public class CardToken {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
    private UUID tokenId;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "vault_card_id", nullable = false)
    private VaultCard vaultCardId;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private UUID merchantId;

    @Column(nullable = false,length = 50,unique = true)
    private String token;

    private LocalDateTime revokedAt;
}
