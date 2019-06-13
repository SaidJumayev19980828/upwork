package com.nasnav.persistence;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.Set;

@Entity
@DiscriminatorValue("1")
@Data
@EqualsAndHashCode
public class BundleEntity extends ProductEntity{
    @ManyToMany(cascade = CascadeType.ALL )
    @JoinTable(name = "product_bundles"
            ,joinColumns = {@JoinColumn(name="product_id")}
            ,inverseJoinColumns = {@JoinColumn(name="bundle_stock_id")})
    private Set<StocksEntity> items;
}
