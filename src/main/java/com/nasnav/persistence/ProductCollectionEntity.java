package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Loader;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static javax.persistence.CascadeType.ALL;

@DiscriminatorValue("2")
@Entity
@Data
@EqualsAndHashCode(callSuper=false)
@SQLDelete(sql = "UPDATE PRODUCTS SET removed = 1 WHERE id = ?")
@Loader(namedQuery = "findCollectionById")
@NamedQuery(name = "findCollectionById", query = "SELECT p FROM ProductEntity p WHERE p.id=?1 AND p.removed = 0")
@Where(clause = "removed = 0")
public class ProductCollectionEntity extends ProductEntity{


    @OneToMany(mappedBy = "collection", cascade = ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<ProductCollectionItemEntity> items;


    public ProductCollectionEntity(){
        items = new HashSet<>();
    }


    public Set<ProductVariantsEntity> getVariants(){
        return items
                .stream()
                .map(ProductCollectionItemEntity::getItem)
                .collect(toSet());
    };


    public void addItem(ProductCollectionItemEntity item){
        items.add(item);
        item.setCollection(this);
    }

}
