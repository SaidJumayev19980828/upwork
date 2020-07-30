package com.nasnav.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.Organization_BrandRepresentationObject;

import lombok.Data;
import lombok.EqualsAndHashCode.Exclude;

@Entity
@Table(name = "brands")
@Data
public class BrandsEntity implements BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "category_id")
    private Integer categoryId;    
       
    @Column(name = "banner_image")
    private String bannerImage;

    @Column(name = "p_name")
    private String pname;
    
    @Column(name = "dark_logo")
    private String darkLogo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonIgnore
    @Exclude
    @lombok.ToString.Exclude
    private OrganizationEntity organizationEntity;


    private String name;    
    private String logo;
    private String description;

    private Integer removed;
    
    
    
    public BrandsEntity() {
    	this.removed = 0;
    }

    @Override
    public BaseRepresentationObject getRepresentation() {
        Organization_BrandRepresentationObject brandRepresentationObject = new Organization_BrandRepresentationObject();
        brandRepresentationObject.setCategoryId(getCategoryId());
        brandRepresentationObject.setId(getId());
        brandRepresentationObject.setLogoUrl(getLogo());
        brandRepresentationObject.setName(getName());
        brandRepresentationObject.setPname(getPname());
        brandRepresentationObject.setBannerImage(getBannerImage());
        return brandRepresentationObject;
    }
}