package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

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
public class ReferralWallet extends BaseReferralEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal balance = new BigDecimal("0.00");

    @Version
    private Integer version;


    public void depositBalance(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public void withdrawBalance(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }


}

