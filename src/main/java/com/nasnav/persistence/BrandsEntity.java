package com.nasnav.persistence;

import java.util.Date;

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
    
    @Column(name = "created_at")
    private Date createdAt;
    
    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(name = "p_name")
    private String pname;
    
    @Column(name = "following_standards")
    private Boolean followingStandards;
           
    @Column(name = "stock_management")
    private Boolean stockManagement;
        
    @Column(name = "google_plus")
    private String googlePlus;
    
    @Column(name = "display_name")
    private String displayName;
    
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
    private String facebook;    
    private String twitter;    
    private String instagram;
    private String description;
    private String pinterest;
    private String youtube;
    private String snapchat;
    

    @Override
    public BaseRepresentationObject getRepresentation() {
        Organization_BrandRepresentationObject brandRepresentationObject = new Organization_BrandRepresentationObject();
        brandRepresentationObject.setCategoryId(getCategoryId());
        brandRepresentationObject.setDisplayName(getDisplayName());
        brandRepresentationObject.setId(getId());
        brandRepresentationObject.setLogoUrl(getLogo());
        brandRepresentationObject.setName(getName());
        brandRepresentationObject.setPname(getPname());
        brandRepresentationObject.setBannerImage(getBannerImage());
        return brandRepresentationObject;
    }
}