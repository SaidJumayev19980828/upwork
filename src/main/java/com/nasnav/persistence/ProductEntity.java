package com.nasnav.persistence;

import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.ProductRepresentationObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DiscriminatorFormula;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "products")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="product_type",  discriminatorType = DiscriminatorType.INTEGER)
@DiscriminatorValue("0")
@DiscriminatorFormula("COALESCE(product_type,0)")        //TODO: we only need this until the Column PRODUCTS.PRODUCT_TYPE is set as non-null
@Data
@EqualsAndHashCode(callSuper=false)
public class ProductEntity extends AbstractPersistable<Long> implements BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "p_name")
    private String pname;

    private String description;

    @Column(name = "cover_image")
    private String coverImage;

    @Column(name = "info_updated")
    private Boolean infoUpdated;

    @Column(name = "item_id")
    private String itemId;

    @Column(name = "created_at")
    private Date creationDate;
    @Column(name = "updated_at")
    private Date updateDate;

    @Column(name="organization_id")
    private Long organizationId;
    
    @Column(name="category_id")
    private Long categoryId;
    
    @Column(name="brand_id")
    private Long brandId;

    @Column(name="product_type")
    private Integer productType = ProductTypes.DEFAULT;

    //TODO : we only need this until the Column PRODUCTS.PRODUCT_TYPE is set as non-null
    public Integer getProductType(){
        return productType == null ? ProductTypes.DEFAULT : productType;
    }

//    @OneToMany(mappedBy="productEntity")
//    @JsonIgnore
//    private Set<StocksEntity> stocksEntities;

//    @OneToOne(mappedBy = "productEntity")
//    @JsonIgnore
//    private ProductVariants productVariants;

//    @JsonIgnore
//    @OneToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "category_id", referencedColumnName = "id")
//    private CategoriesEntity categoriesEntity;

//    @JsonIgnore
//    @OneToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "brand_id", referencedColumnName = "id")
//    private BrandsEntity brandsEntity;
    
//    @ManyToMany(fetch = FetchType.LAZY)
//    @JoinColumn(name = "organization_id", nullable = false)
//    @JsonIgnore
//    private OrganizationEntity organizationEntity;

    @Override
    public BaseRepresentationObject getRepresentation() {
        ProductRepresentationObject productRepresentationObject = new ProductRepresentationObject();
        productRepresentationObject.setId(getId());
        productRepresentationObject.setImageUrl(getCoverImage());
        productRepresentationObject.setName(getName());
        productRepresentationObject.setPname(getPname());
        productRepresentationObject.setCategoryId(getCategoryId());
        productRepresentationObject.setBrandId(getBrandId());

        return productRepresentationObject;
    }
}