package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "referral_wallet")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ReferralWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Version
    private Integer version;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserEntity user;


    @OneToMany(mappedBy = "referralWallet", cascade = CascadeType.ALL, orphanRemoval = true ,fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<ReferralWalletTransaction> transactions= new HashSet<>();




    public void addTransactions(ReferralWalletTransaction transactions) {
        if (transactions != null) {
            if (!getTransactions().contains(transactions))
                getTransactions().add(transactions);
            transactions.setReferralWallet(this);
        }
    }


    public void depositBalance(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public void withdrawBalance(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }


}

