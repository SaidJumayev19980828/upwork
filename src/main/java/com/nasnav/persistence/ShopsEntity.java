package com.nasnav.persistence;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.springframework.data.jpa.domain.AbstractPersistable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.Address;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.ShopRepresentationObject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Table(name = "shops")
@Entity
@Data
@EqualsAndHashCode(callSuper=false)
public class ShopsEntity extends AbstractPersistable<Long> implements BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    
    @Column(name = "p_name")
    private String pname;

    private String country;
    private String city;
    private String zip;
    private String street;
    
    @Column(name = "street_number")
    private String streetNumber;
    
    private String floor;
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    private String area;
    
    @Column(name = "p_area")
    private String parea;
    
    @Column(precision=10, scale=2)
    private BigDecimal lat;
    
    @Column(precision=10, scale=2)
    private BigDecimal lng;
    
    @Column(name = "brand_id")
    private Long brandId;
    @Column(name = "created_at")
    private Date createdAt;
    
    @Column(name = "updated_at")
    private Date updatedAt = new Date();

    @Column(name = "p_street")
    private String pStreet;


    private String logo;

    private String banner;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "mall_id", referencedColumnName = "id")
    @JsonIgnore
    private MallsEntity mallsEntity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonIgnore
    private OrganizationEntity organizationEntity;



    @Override
    public BaseRepresentationObject getRepresentation() {
        ShopRepresentationObject shopRepresentationObject = new ShopRepresentationObject();
        shopRepresentationObject.setId(getId());
        shopRepresentationObject.setLogo(getLogo());
        shopRepresentationObject.setBanner(getBanner());
        shopRepresentationObject.setName(getName());
        shopRepresentationObject.setPname(getPname());
        Address address = new Address();
        address.setArea(getArea());
        address.setParea(getParea());
        address.setCity(getCity());
        address.setCountry(getCountry());
        address.setStreet(getStreet());
        address.setFloor(getFloor());
        address.setLat(String.valueOf(getLat()));
        address.setLng(String.valueOf(getLng()));

        shopRepresentationObject.setAddress(address);
        //TODO database to support from to time as multiple duration through day
        shopRepresentationObject.setOpenWorkingDays(null);
        return shopRepresentationObject;
    }
}
