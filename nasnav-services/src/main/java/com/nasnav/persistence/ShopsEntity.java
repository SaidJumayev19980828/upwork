package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.AddressRepObj;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.ShopRepresentationObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;
import java.util.Set;

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

    @Column(name = "dark_logo")
    private String darkLogo;

    private String banner;

    private Integer removed;

    @Column(name = "google_place_id")
    private String placeId;

    private String code;

    @Column(name = "allow_other_points")
    private Boolean allowOtherPoints;

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

    private Integer priority;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "shopsEntity")
    @JsonIgnore
    @Exclude
    @lombok.ToString.Exclude
    private Set<ShopThreeSixtyEntity> shop360s;


    @Column(name = "yeshtery_state")
    private Integer yeshteryState;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_topic")
    private NotificationTopicEntity notificationTopic;

    public ShopsEntity() {
    	this.isWarehouse = 0;
    }

    @Override
    public BaseRepresentationObject getRepresentation() {
        ShopRepresentationObject obj = new ShopRepresentationObject();
        BeanUtils.copyProperties(this, obj);

        obj.setIsWarehouse(getIsWarehouse() > 0);
        obj.setHas360(!(shop360s == null || shop360s.isEmpty()));
        if (getAddressesEntity() != null) {
            obj.setAddress((AddressRepObj) getAddressesEntity().getRepresentation());
        }
        //TODO why working days won't be returned from the API unlike getShopById API
        //TODO database to support from to time as multiple duration through day
        obj.setOpenWorkingDays(null);
        return obj;
    }
}
