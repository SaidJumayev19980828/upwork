package com.nasnav.persistence;

import java.time.LocalDateTime;
import java.util.Set;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.NamedNativeQuery;
import javax.persistence.Table;

import com.nasnav.dto.Pair;
import org.hibernate.annotations.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


@SqlResultSetMapping(
        name = "Pair",
        classes = @ConstructorResult(
                targetClass = Pair.class,
                columns = {
                        @ColumnResult(name = "product_id", type = long.class),
                        @ColumnResult(name = "tag_id", type = long.class)
                }))
@NamedNativeQuery(
        name = "ProductEntity.getProductTags",
        query = "SELECT t.product_id, t.tag_id FROM Product_tags t WHERE t.product_id in :productsIds and t.tag_id in :tagsIds",
        resultSetMapping = "Pair"
)

@Entity
@Table(name = "products")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="product_type",  discriminatorType = DiscriminatorType.INTEGER)
@DiscriminatorValue("0")
@DiscriminatorFormula("COALESCE(product_type,0)")        //TODO: we only need this until the Column PRODUCTS.PRODUCT_TYPE is set as non-null
@Data
@EqualsAndHashCode(callSuper=false)
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "p_name")
    private String pname;

    private String description;


    @Column(name = "info_updated")
    private Boolean infoUpdated;

    @Column(name = "item_id")
    private String itemId;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime creationDate;
    
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updateDate;

    @Column(name="organization_id")
    private Long organizationId;
    
    @Column(name="category_id")
    private Long categoryId;
    
    @Column(name="brand_id")
    private Long brandId;

    @Column(name="barcode")
    private String barcode;

    @Column(name="product_type")
    private Integer productType = ProductTypes.DEFAULT;
    

    //TODO : we only need this until the Column PRODUCTS.PRODUCT_TYPE is set as non-null
    public Integer getProductType(){
        return productType == null ? ProductTypes.DEFAULT : productType;
    }
    
    

    @OneToMany(mappedBy = "productEntity")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<ProductVariantsEntity> productVariants;


    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JoinTable(name = "product_tags"
            ,joinColumns = {@JoinColumn(name="product_id")}
            ,inverseJoinColumns = {@JoinColumn(name="tag_id")})
    private Set<OrganizationTagsEntity> tags;

    public void insertProductTag(OrganizationTagsEntity tag) {
        this.tags.add(tag);
    }

    public void removeProductTag(OrganizationTagsEntity tag) {
        this.tags.remove(tag);
    }
}