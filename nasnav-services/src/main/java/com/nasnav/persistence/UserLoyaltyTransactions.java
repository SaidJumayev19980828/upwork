package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static javax.persistence.FetchType.LAZY;


@Entity(name = "user_loyalty_transactions")
@Table(name = "user_loyalty_transactions")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class UserLoyaltyTransactions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT now()")
    private LocalDateTime createdAt;

    @Column(name = "type", nullable = false, length = 255)
    private String type;

    @Column(name = "description", length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meta_order_id", foreignKey = @ForeignKey(name = "fk_meta_order_id"))
    @JsonIgnore
    private MetaOrderEntity metaOrder;


    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "shop_id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private ShopsEntity shop;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "org_id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private OrganizationEntity organization;

    @OneToOne(fetch = LAZY)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private OrdersEntity order;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_loyalty_points", nullable = false, foreignKey = @ForeignKey(name = "fk_user_loyalty_points"))
    private UserLoyaltyPoints userLoyaltyPoints;

}
