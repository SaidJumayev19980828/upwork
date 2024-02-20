package com.nasnav.persistence;

import com.nasnav.enumerations.ReferralTransactionsType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static java.lang.String.format;

@Entity
@Table(name = "referral_transactions")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ReferralTransactions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "referral_transaction_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReferralTransactionsType type;

    @Column(name = "order_id")
    private Long orderId;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "referral_wallet_id", referencedColumnName = "id")
    private ReferralWallet referralWallet;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "referral_id", referencedColumnName = "id")
    private ReferralCodeEntity referralCodeEntity;

}

