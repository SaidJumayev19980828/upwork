package com.nasnav.persistence;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Table(name = "advertisement_product")
@Entity
@Getter
@Setter
public class AdvertisementProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ProductEntity product;

    @ManyToOne
    @JoinColumn(name = "advertisement_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private AdvertisementEntity advertisement;

    @OneToMany(mappedBy = "advertisementProduct",cascade = CascadeType.ALL, orphanRemoval = true
            ,fetch = FetchType.EAGER
    )
    private Set<AdvertisementProductCompensation> compensationRules = new HashSet<>();

    @Column(name = "coins")
    private Integer coins;

    @Column(name = "likes")
    private Integer likes;


    public void addCompensationRule(AdvertisementProductCompensation compensationRule) {
        compensationRule.setAdvertisementProduct(this);
        compensationRules.add(compensationRule);
    }

}
