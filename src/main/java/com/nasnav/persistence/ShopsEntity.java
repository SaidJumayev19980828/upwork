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
import com.nasnav.dto.AddressRepObj;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.ShopRepresentationObject;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Table(name = "shops")
@Entity
@Data
@EqualsAndHashCode(callSuper=false)
@SQLDelete(sql = "UPDATE Shops SET removed = 1 WHERE id = ?")
@Where(clause = "removed = 0")
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

    @Override
    public BaseRepresentationObject getRepresentation() {
        ShopRepresentationObject shopRepresentationObject = new ShopRepresentationObject();

        shopRepresentationObject.setId(getId());
        shopRepresentationObject.setLogo(getLogo());
        shopRepresentationObject.setBanner(getBanner());
        shopRepresentationObject.setName(getName());
        shopRepresentationObject.setPname(getPname());

        if (getAddressesEntity() != null) {
            shopRepresentationObject.setAddress((AddressRepObj) getAddressesEntity().getRepresentation());
        }
        //TODO database to support from to time as multiple duration through day
        shopRepresentationObject.setOpenWorkingDays(null);
        return shopRepresentationObject;
    }
}
