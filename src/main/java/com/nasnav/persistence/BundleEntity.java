package com.nasnav.persistence;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@DiscriminatorValue("1")
@Data
@EqualsAndHashCode(callSuper=false)
public class BundleEntity extends ProductEntity{
    @ManyToMany
    @JoinTable(name = "product_bundles"
            ,joinColumns = {@JoinColumn(name="product_id")}
            ,inverseJoinColumns = {@JoinColumn(name="item_product_id")})
    private Set<ProductEntity> productItems;
    
    
    @ManyToMany
    @JoinTable(name = "product_bundles"
            ,joinColumns = {@JoinColumn(name="product_id")}
            ,inverseJoinColumns = {@JoinColumn(name="item_variant_id")})
    private Set<ProductVariantsEntity> variantItems;
    
    
    @OneToMany( mappedBy = "productEntity", cascade = CascadeType.REMOVE)
    private Set<StocksEntity> bundleVirtualStockItem;
}
