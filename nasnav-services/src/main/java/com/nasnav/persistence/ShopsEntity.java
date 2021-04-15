package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.AddressRepObj;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.ShopRepresentationObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;

import javax.persistence.*;

@Table(name = "shops")
@Entity
@Data
@EqualsAndHashCode(callSuper=false)
public class ShopsEntity implements BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    
    @Column(name = "p_name")
    private String pname;
    
    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "brand_id")
    private Long brandId;

    private String logo;

    private String banner;

    private Integer removed;

    @Column(name = "google_place_id")
    private String placeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonIgnore
    @Exclude
    @lombok.ToString.Exclude
    private OrganizationEntity organizationEntity;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "address_id")
    @JsonIgnore
    @Exclude
    @lombok.ToString.Exclude
    private AddressesEntity addressesEntity;
     
    @Column(name = "is_warehouse")
    private Integer isWarehouse;
    
    
    public ShopsEntity() {
    	this.isWarehouse = 0;
    }

    @Override
    public BaseRepresentationObject getRepresentation() {
        ShopRepresentationObject shopRepresentationObject = new ShopRepresentationObject();

        shopRepresentationObject.setId(getId());
        shopRepresentationObject.setLogo(getLogo());
        shopRepresentationObject.setBanner(getBanner());
        shopRepresentationObject.setName(getName());
        shopRepresentationObject.setPname(getPname());
        shopRepresentationObject.setPlaceId(getPlaceId());
        shopRepresentationObject.setIsWarehouse(getIsWarehouse() > 0);

        if (getAddressesEntity() != null) {
            shopRepresentationObject.setAddress((AddressRepObj) getAddressesEntity().getRepresentation());
        }
        //TODO why working days won't be returned from the API unlike getShopById API
        //TODO database to support from to time as multiple duration through day
        shopRepresentationObject.setOpenWorkingDays(null);
        return shopRepresentationObject;
    }
}
