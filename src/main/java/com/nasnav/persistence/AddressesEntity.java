package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.AddressRepObj;
import com.nasnav.dto.BaseRepresentationObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Set;


@Table(name = "addresses")
@Entity
@Data
@EqualsAndHashCode(callSuper=false)
public class AddressesEntity implements BaseEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flat_number")
    private String flatNumber;

    @Column(name = "building_number")
    private String buildingNumber;

    @Column(name = "address_line_1")
    private String addressLine1;

    @Column(name = "address_line_2")
    private String addressLine2;

    @Column(precision=10, scale=2)
    private BigDecimal latitude;

    @Column(precision=10, scale=2)
    private BigDecimal longitude;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "postal_code")
    private String postalCode;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "area_id", referencedColumnName = "id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private AreasEntity areasEntity;


    @ManyToMany(mappedBy = "addresses")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private Set<UserEntity> users;

    @Override
    public BaseRepresentationObject getRepresentation() {
        AddressRepObj address = new AddressRepObj();

        BeanUtils.copyProperties(this, address);

        if (areasEntity != null) {
            address.setArea(areasEntity.getName());
            if (areasEntity.getCitiesEntity() != null) {
                address.setCity(areasEntity.getCitiesEntity().getName());
                if (areasEntity.getCitiesEntity().getCountriesEntity() != null) {
                    address.setCountry(areasEntity.getCitiesEntity().getCountriesEntity().getName());
                }
            }
        }

        return address;
    }
}
