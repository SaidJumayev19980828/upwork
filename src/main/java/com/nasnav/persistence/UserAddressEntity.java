package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.AddressRepObj;
import com.nasnav.dto.BaseRepresentationObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;
import java.util.Set;

@Table(name = "user_addresses")
@Entity
@Data
@EqualsAndHashCode(callSuper=false)
public class UserAddressEntity implements BaseEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity user;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private AddressesEntity address;

    private boolean principal;


    @Override
    public BaseRepresentationObject getRepresentation() {
        AddressRepObj obj = new AddressRepObj();
        BeanUtils.copyProperties(this.address, obj);
        obj.setPrincipal(this.principal);
        AreasEntity area = this.address.getAreasEntity();
        if (area != null) {
            obj.setAreaId(area.getId());
            obj.setArea(area.getName());
            CitiesEntity city = area.getCitiesEntity();
            if (city != null) {
                obj.setCity(city.getName());
                if (city.getCountriesEntity() != null) {
                    obj.setCountry(city.getCountriesEntity().getName());
                }
            }
        }
        return obj;
    }
}
