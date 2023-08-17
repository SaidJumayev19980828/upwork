package com.nasnav.persistence;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Table(name = "bank_accounts")
@Entity
@Data
public class BankAccountEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToOne
    @JoinColumn(name = "org_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private OrganizationEntity organization;

    @OneToOne
    @JoinColumn(name = "user_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity user;

    @Column(name = "wallet_address")
    private String walletAddress;

    @Column(name = "opening_balance")
    private Float openingBalance;

    @OneToOne
    @JoinColumn(name = "opening_balance_activity_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private BankAccountActivityEntity openingBalanceActivity;

    @Column(name = "opening_balance_date")
    private LocalDateTime openingBalanceDate;

    @Column(name = "locked")
    private Boolean locked;
}
