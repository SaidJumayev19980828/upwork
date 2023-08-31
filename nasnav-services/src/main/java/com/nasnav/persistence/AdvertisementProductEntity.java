package com.nasnav.persistence;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

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

    @Column(name = "coins")
    private Integer coins;

    @Column(name = "likes")
    private Integer likes;
}
