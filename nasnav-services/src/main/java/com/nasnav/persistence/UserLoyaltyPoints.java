package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Version;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.FetchType.EAGER;


@Entity(name = "user_loyalty_points")
@Table(name = "user_loyalty_points")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class UserLoyaltyPoints {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "balance", nullable = false, columnDefinition = "NUMERIC DEFAULT 0.0")
    private BigDecimal balance;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT now()")
    private LocalDateTime createdAt;

    @Column(name = "version")
    @Version
    private Integer version;

    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private UserEntity user;

    @OneToMany(mappedBy = "userLoyaltyPoints", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserLoyaltyTransactions> transactions = new HashSet<>();


    public void addTransactions(UserLoyaltyTransactions transactions) {
        if (transactions != null) {
            getTransactions().add(transactions);
            transactions.setUserLoyaltyPoints(this);
        }
    }


    public void depositPoints(BigDecimal amount) {
        this.balance = (this.balance == null) ? amount : this.balance.add(amount);
    }


    public void withdrawBalance(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }


}
