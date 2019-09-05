package com.nasnav.persistence;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@DiscriminatorValue("1")
@Data
@EqualsAndHashCode(callSuper=true)
public class BundleEntity extends ProductEntity{
    @ManyToMany
    @JoinTable(name = "product_bundles"
            ,joinColumns = {@JoinColumn(name="product_id")}
            ,inverseJoinColumns = {@JoinColumn(name="bundle_stock_id")})
    private Set<StocksEntity> items;    
   
}
