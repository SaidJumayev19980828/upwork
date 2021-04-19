package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.Organization_BrandRepresentationObject;
import lombok.Data;
import lombok.EqualsAndHashCode.Exclude;

import javax.persistence.*;

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

    @Column(name = "cover_url")
    private String coverUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    @JsonIgnore
    @Exclude
    @lombok.ToString.Exclude
    private OrganizationEntity organizationEntity;


    private String name;    
    private String logo;
    private String description;

    private Integer removed;

    private Integer priority;
    
    public BrandsEntity() {
    	this.removed = 0;
    	this.priority = 0;
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
        brandRepresentationObject.setCoverUrl(getCoverUrl());
        brandRepresentationObject.setPriority(getPriority());
        return brandRepresentationObject;
    }
}