package com.nasnav.persistence;

import com.nasnav.enumerations.WalletTransactions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static java.lang.String.format;

@Entity
@Table(name = "referral_wallet_transactions")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ReferralWalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private WalletTransactions type;

    private String description;

    @ManyToOne
    @JoinColumn(name = "referral_wallet_id")
    private ReferralWallet referralWallet;




    public static String recordOpeningBalanceDeposit(BigDecimal amount) {
        return format("Deposited opening balance into Referral Wallet: %s", amount);
    }

    public static String recordRevenueDeposit(BigDecimal amount) {
        return format("Deposited Referral Revenue: %s", amount);
    }

    public static String recordWithdraw(BigDecimal amount) {
        return format("Withdraw Referral amount : %s", amount);
    }


    public void setDescription(boolean openingBalance) {
        switch (type) {
            case DEPOSIT:
                this.description = openingBalance ? recordOpeningBalanceDeposit(amount) : recordRevenueDeposit(amount);
                break;
            case WITHDRAWAL:
                this.description = recordWithdraw(amount);
                break;
        }
    }



}

