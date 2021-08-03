package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.Pair;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static lombok.AccessLevel.NONE;


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
@NamedNativeQuery(
        name = "ProductEntity.getTagsByProductIdIn",
        query = "select t.product_id, t.tag_id from Product_tags t where t.product_id in :ids",
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
@SQLDelete(sql = "UPDATE PRODUCTS SET removed = 1 WHERE id = ?")
@Loader(namedQuery = "findProductById")
@NamedQuery(name = "findProductById", query = "SELECT p FROM ProductEntity p WHERE p.id=?1 AND p.removed = 0")
@Where(clause = "removed = 0")
public class ProductEntity {

	public ProductEntity() {
	    // these values have non-null constraint and default values in db
        // but Hibernate sets their value null explicitly if not provided in the code
		removed = 0;
		priority = 0;
        hide = false;
		tags = new HashSet<>();
	}
	

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "p_name")
    private String pname;

    private String description;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime creationDate;
    
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updateDate;

    @Column(name="organization_id")
    private Long organizationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "brand_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private BrandsEntity brand;

    @Column(name="category_id")
    @Setter(value = NONE)
    private Long categoryId;

    @Column(name="barcode")
    private String barcode;

    @Column(name="product_type")
    private Integer productType = ProductTypes.DEFAULT;
    

    @Column(name="removed")
    private Integer removed;
    
    @Column(name="hide")
    private Boolean hide;

    @Column(name="search_360")
    private Boolean search360;

    private Integer priority;

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
    private Set<TagsEntity> tags;

    public void insertProductTag(TagsEntity tag) {
        this.tags.add(tag);
    }

    public void removeProductTag(TagsEntity tag) {
        this.tags.remove(tag);
    }
}