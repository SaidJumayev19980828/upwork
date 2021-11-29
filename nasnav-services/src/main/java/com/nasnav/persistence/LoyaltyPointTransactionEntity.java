package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static javax.persistence.FetchType.LAZY;

@Data
@Entity
@Table(name = "loyalty_point_transactions")
@EqualsAndHashCode(callSuper=false)
public class LoyaltyPointTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    private BigDecimal points;

    private BigDecimal amount;

    @Column(name = "is_valid")
    private Boolean isValid;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private UserEntity user;

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

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "loyalty_point_id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private LoyaltyPointEntity loyaltyPoint;

    @Column(name = "got_online")
    private Boolean gotOnline;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "charity_id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private LoyaltyCharityEntity charity;

    @Column(name = "is_donate")
    private Boolean isDonate;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "gift_id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private LoyaltyGiftEntity gift;

    @Column(name = "is_gift")
    private Boolean isGift;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "coins_drop_id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private LoyaltyCoinsDropEntity coinsDrop;

    @Column(name = "is_coins_drop")
    private Boolean isCoinsDrop;
}
