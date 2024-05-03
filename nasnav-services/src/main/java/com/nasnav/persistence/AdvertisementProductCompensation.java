package com.nasnav.persistence;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Table(name = "advertisement_product_compensation")
@Entity
@Getter
@Setter
public class AdvertisementProductCompensation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advertisement_product_id", nullable = false)
    private AdvertisementProductEntity advertisementProduct;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "compensation_rule", nullable = false)
    private CompensationRulesEntity compensationRule;
}
