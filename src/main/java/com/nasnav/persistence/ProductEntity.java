package com.nasnav.persistence;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.ProductRepresentationObject;

import lombok.Data;

@Entity
@Table(name = "products")
@Data
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
    private Date createdAt;
    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(name="organization_id")
    private Long organizationId;
    
    @Column(name="category_id")
    private Long categoryId;
    
    @Column(name="brand_id")
    private Long brandId;
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