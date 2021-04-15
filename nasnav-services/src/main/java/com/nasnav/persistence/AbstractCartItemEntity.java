package com.nasnav.persistence;

import lombok.Data;

import javax.persistence.*;


@Entity
@Table(name = "CART_ITEMS")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="is_wishlist",  discriminatorType = DiscriminatorType.INTEGER)
@Data
public class AbstractCartItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="COVER_IMAGE")
    private String coverImage;

    @Column(name="VARIANT_FEATURES")
    private String variantFeatures;

    private Integer quantity;

    @ManyToOne
    @JoinColumn(name="STOCK_ID", referencedColumnName = "ID")
    private StocksEntity stock;

    @ManyToOne
    @JoinColumn(name="USER_ID")
    private UserEntity user;

    @Column(name="ADDITIONAL_DATA")
    private String additionalData;
}
