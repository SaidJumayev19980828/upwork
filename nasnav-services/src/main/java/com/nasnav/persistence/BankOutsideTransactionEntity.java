package com.nasnav.persistence;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Table(name = "bank_outside_transactions")
@Entity
@Data
public class BankOutsideTransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "account_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private BankAccountEntity account;

    @Column(name = "amount_in")
    private Float amountIn;

    @Column(name = "amount_out")
    private Float amountOut;

    @Column(name = "activity_date")
    private LocalDateTime activityDate;

    @Column(name = "bc_key", unique = true)
    private String bcKey;
}
