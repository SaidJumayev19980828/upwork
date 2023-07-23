package com.nasnav.persistence;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Table(name = "bank_account_activities")
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BankAccountActivityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "account_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private BankAccountEntity account;

    @Column(name = "amount_in")
    private Long amountIn;

    @Column(name = "amount_out")
    private Long amountOut;

    @Column(name = "activity_date")
    private LocalDateTime activityDate;

    @OneToOne
    @JoinColumn(name = "inside_transaction_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private BankInsideTransactionEntity bankInsideTransaction;

    @OneToOne
    @JoinColumn(name = "outside_transaction_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private BankOutsideTransactionEntity bankOutsideTransaction;


}
