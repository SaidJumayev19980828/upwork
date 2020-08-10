package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Loader;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Set;

@DiscriminatorValue("2")
@Entity
@Data
@EqualsAndHashCode(callSuper=false)
@SQLDelete(sql = "UPDATE PRODUCTS SET removed = 1 WHERE id = ?")
@Loader(namedQuery = "findCollectionById")
@NamedQuery(name = "findCollectionById", query = "SELECT p FROM ProductEntity p WHERE p.id=?1 AND p.removed = 0")
@Where(clause = "removed = 0")
public class ProductCollectionEntity extends ProductEntity{

    @ManyToMany
    @JoinTable(name = "product_collections"
            ,joinColumns = {@JoinColumn(name="product_id")}
            ,inverseJoinColumns = {@JoinColumn(name="variant_id")})
    private Set<ProductVariantsEntity> variants;

}
