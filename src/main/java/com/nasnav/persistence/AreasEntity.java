package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.AreasRepObj;
import com.nasnav.dto.BaseRepresentationObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;


@Table(name = "areas")
@Entity
@Data
@EqualsAndHashCode(callSuper=false)
public class AreasEntity implements BaseEntity {

    @Id
    private Long id;

    private String name;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "city_id", referencedColumnName = "id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private CitiesEntity citiesEntity;

    @Override
    public BaseRepresentationObject getRepresentation() {
        AreasRepObj obj = new AreasRepObj();
        obj.setId(getId());
        obj.setName(getName());

        return obj;
    }
}
