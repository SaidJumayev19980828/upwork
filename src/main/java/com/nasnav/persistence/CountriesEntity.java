package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.AreasRepObj;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.CitiesRepObj;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.Set;

@Table(name = "countries")
@Entity
@Data
public class CountriesEntity implements BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;

    @OneToMany(mappedBy = "countriesEntity", cascade = CascadeType.REMOVE)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<CitiesEntity> cities;

    @Override
    public BaseRepresentationObject getRepresentation() {
        return null;
    }

}
