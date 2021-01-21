package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.SubAreasRepObj;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.math.BigDecimal;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "SUB_AREAS")
@Data
@EqualsAndHashCode(callSuper=false)
public class SubAreasEntity implements BaseEntity{

    @Id @GeneratedValue
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "longitude")
    private BigDecimal longitude;

    @Column(name = "latitude")
    private BigDecimal latitude;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "area_id", referencedColumnName = "id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private AreasEntity area;


    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private OrganizationEntity organization;


    @Override
    public BaseRepresentationObject getRepresentation() {
        SubAreasRepObj obj = new SubAreasRepObj();
        obj.setId(getId());
        obj.setName(getName());

        return obj;
    }
}
