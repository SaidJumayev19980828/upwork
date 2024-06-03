package com.nasnav.persistence;

import com.nasnav.enumerations.ReferralTransactionsType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;


@Entity
@Table(name = "referral_transactions")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ReferralTransactions extends BaseReferralEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "referral_transaction_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReferralTransactionsType type;

    @Column(name = "order_id")
    private Long orderId;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "referral_wallet_id", referencedColumnName = "id")
    private ReferralWallet referralWallet;

    @ManyToOne
    @JoinColumn(name = "referral_id", referencedColumnName = "id")
    private ReferralCodeEntity referralCodeEntity;

}

