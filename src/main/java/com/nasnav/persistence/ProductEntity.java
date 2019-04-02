package com.nasnav.persistence;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.ProductRepresentationObject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "products")
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
    private Date creationdDate;
    @Column(name = "updated_at")
    private Date updateDate;

    @Column(name="organization_id")
    private Long organizationId;
    
    @Column(name="category_id")
    private Long categoryId;
    
    @Column(name="brand_id")
    private Long brandId;
    
    @OneToMany(mappedBy="productEntity")
    @JsonIgnore
    private Set<StocksEntity> stocksEntities;
    
    @OneToOne(mappedBy = "productEntity")
    @JsonIgnore
    private ProductVariants productVariants;
	
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
        productRepresentationObject.setPName(getPname());

        return productRepresentationObject;
    }
}