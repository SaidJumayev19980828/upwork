package com.nasnav.persistence;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Loader;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Set;

@Entity
@DiscriminatorValue("1")
@Data
@EqualsAndHashCode(callSuper=true)
@SQLDelete(sql = "UPDATE PRODUCTS SET removed = 1 WHERE id = ?")
@Loader(namedQuery = "findBundleById")
@NamedQuery(name = "findBundleById", query = "SELECT p FROM ProductEntity p WHERE p.id=?1 AND p.removed = 0")
@Where(clause = "removed = 0")
public class BundleEntity extends ProductEntity{
    @ManyToMany
    @JoinTable(name = "product_bundles"
            ,joinColumns = {@JoinColumn(name="product_id")}
            ,inverseJoinColumns = {@JoinColumn(name="bundle_stock_id")})
    private Set<StocksEntity> items;    
   
}
