package com.nasnav.persistence;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Table(name = "bank_inside_transactions")
@Entity
@Data
public class BankInsideTransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "sender_account_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private BankAccountEntity sender;

    @OneToOne
    @JoinColumn(name = "receiver_account_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private BankAccountEntity receiver;

    @Column(name = "amount")
    private Long amount;

    @Column(name = "activity_date")
    private LocalDateTime activityDate;
}
