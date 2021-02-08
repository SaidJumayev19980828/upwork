package com.nasnav.persistence;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Entity
@Data
@Table(name = "product_collections")
public class ProductCollectionItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "priority")
    private Integer priority;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "product_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ProductCollectionEntity collection;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariantsEntity item;
}
