package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.Pair;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DiscriminatorFormula;
import org.hibernate.annotations.Loader;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;
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
@SqlResultSetMapping(
        name = "addonPair",
        classes = @ConstructorResult(
                targetClass = Pair.class,
                columns = {
                        @ColumnResult(name = "product_id", type = long.class),
                        @ColumnResult(name = "addon_id", type = long.class)
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
@NamedNativeQuery(
        name = "ProductEntity.getProductAddons",
        query = "SELECT t.product_id, t.addon_id FROM Product_addons t WHERE t.product_id in :productsIds and t.addon_id in :addonsIds",
        resultSetMapping = "addonPair"
)

       
@Entity
@Table(name = "products")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="product_type",  discriminatorType = DiscriminatorType.INTEGER)
@DiscriminatorValue("0")
@DiscriminatorFormula("product_type")
@Setter
@Getter
@AllArgsConstructor
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
        allowReward = false;
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
    @Column(name = "model_id")
    private Long modelId;

    private Integer priority;

    @Column(name = "allow_reward")
    private Boolean allowReward;

    @OneToOne
    @JoinColumn(name = "minimum_tier_id", referencedColumnName = "id")
    @JsonIgnore
    private LoyaltyTierEntity minimumTier;

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
    
    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JoinTable(name = "product_addons"
            ,joinColumns = {@JoinColumn(name="product_id")}
            ,inverseJoinColumns = {@JoinColumn(name="addon_id")})
    private Set<AddonEntity> addons;


    public void insertProductTag(TagsEntity tag) {
        this.tags.add(tag);
    }

    public void removeProductTag(TagsEntity tag) {
        this.tags.remove(tag);
    }
    public void insertProductAddon(AddonEntity addon) {
        this.addons.add(addon);
    }

    public void removeProductAddon(AddonEntity addon) {
        this.addons.remove(addon);
    }
}