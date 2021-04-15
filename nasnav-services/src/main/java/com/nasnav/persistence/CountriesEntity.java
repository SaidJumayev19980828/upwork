package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.CountriesRepObj;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Table(name = "countries")
@Entity
@Data
public class CountriesEntity implements BaseEntity, Serializable {

    @Id
    private Long id;
    
    private String name;

    @Column(name = "iso_code")
    private Integer isoCode;

    private String currency;

    @OneToMany(mappedBy = "countriesEntity", cascade = CascadeType.REMOVE)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<CitiesEntity> cities;

    @Override
    public BaseRepresentationObject getRepresentation() {
        CountriesRepObj obj = new CountriesRepObj();
        obj.setId(getId());
        obj.setName(getName());

        return obj;
    }

}
