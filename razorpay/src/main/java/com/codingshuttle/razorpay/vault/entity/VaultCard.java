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
@Table(name = "vault_card")

public class VaultCard {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
    private UUID id;

    @Column(nullable = false,length = 4)
    private String lastFourDigits;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false,length = 6)
    private String bin; // Bank Identification Number, first 6 digits of the card, can be used for card type and issuer identification

    @Column(nullable = false)
    private byte[] encryptedPan;

    @Column(nullable = false)
    private byte[] encryptedDek;// Secret key to encrypt/decrypt PAN, should be stored securely and separately from encrypted PAN

    @Column(nullable = false)
    private int expirationMonth;

    @Column(nullable = false)
    private int expirationYear;

    @Column(nullable = false)
    private String cardholderName;

    private LocalDateTime deletedAt;

}
