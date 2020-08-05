package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.AreasRepObj;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.CitiesRepObj;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.*;

@Table(name = "cities")
@Entity
@Data
public class CitiesEntity implements BaseEntity{

    @Id
    private Long id;
    
    private String name;

    @OneToMany(mappedBy = "citiesEntity", cascade = CascadeType.REMOVE)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<AreasEntity> areas;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "country_id", referencedColumnName = "id")
    @JsonIgnore
    private CountriesEntity countriesEntity;

    @Override
    public BaseRepresentationObject getRepresentation() {
        CitiesRepObj obj = new CitiesRepObj();

        obj.setId(getId());
        obj.setName(getName());

        return obj;
    }
}
