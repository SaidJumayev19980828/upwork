package com.nasnav.persistence;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Table(name = "post_transactions")
@Entity
@Getter
@Setter
public class PostTransactions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private PostEntity post;


    @Column(name = "paid_coins")
    private Long paidCoins=0L;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;


}
