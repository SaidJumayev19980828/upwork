package com.nasnav.persistence;

import java.time.LocalDateTime;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DiscriminatorFormula;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.AbstractPersistable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.ProductRepresentationObject;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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



    @Override
    public ProductRepresentationObject getRepresentation() {
        ProductRepresentationObject productRepresentationObject = new ProductRepresentationObject();
        productRepresentationObject.setId(getId());
        productRepresentationObject.setImageUrl(getCoverImage());
        productRepresentationObject.setName(getName());
        productRepresentationObject.setPname(getPname());
        productRepresentationObject.setCategoryId(getCategoryId());
        productRepresentationObject.setBrandId(getBrandId());
        productRepresentationObject.setBarcode(getBarcode());
        return productRepresentationObject;
    }
}