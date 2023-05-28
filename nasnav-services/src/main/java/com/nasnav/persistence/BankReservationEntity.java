package com.nasnav.persistence;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Table(name = "bank_reservations")
@Entity
@Data
public class BankReservationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "account_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private BankAccountEntity account;

    @Column(name = "amount")
    private Long amount;

    @Column(name = "activity_date")
    private LocalDateTime activityDate;

    @Column(name = "fulfilled")
    private Boolean fulfilled;

    @Column(name = "fulfilled_date")
    private LocalDateTime fulfilledDate;
}
