package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.Organization_BrandRepresentationObject;
import lombok.Data;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "brands")
@Data
public class BrandsEntity implements BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "category_id")
    private Integer categoryId;
    private String name;
    private String logo;
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
//    @Type(type = "com.nasnav.persistence.GenericArrayType")
//    private String[] categories;
    private String facebook;
    private String twitter;
    private String instagram;
    @Column(name = "stock_management")
    private Boolean stockManagement;
    private String description;
    private String pinterest;
    private String youtube;
    private String snapchat;
    @Column(name = "google_plus")
    private String googlePlus;
/*
    @Column(name = "sat")
    private Boolean saturday;
    @Column(name = "sun")
    private Boolean sunday;
    @Column(name = "mon")
    private Boolean monday;
    @Column(name = "tue")
    private Boolean tuesday;
    @Column(name = "wed")
    private Boolean wednesday;
    @Column(name = "thu")
    private Boolean thursday;
    @Column(name = "fri")
    private Boolean friday;
*/
/*
    @Type(type = "com.nasnav.persistence.GenericArrayType")
    private String[] websites;
    @Column(name = "phone_numbers")
    @Type(type = "com.nasnav.persistence.GenericArrayType")
    private String[] phoneNumbers;
*/
    @Column(name = "display_name")
    private String displayName;
    @Column(name = "dark_logo")
    private String darkLogo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonIgnore
    private OrganizationEntity organizationEntity;


//    @JsonIgnore
//    @OneToOne(mappedBy = "brandsEntity")
//    private  ProductEntity productEntity;

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