package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.ThemeClassDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.Set;

@Table(name = "theme_classes")
@Entity
@Data
@EqualsAndHashCode(callSuper=false)
public class ThemeClassEntity extends AbstractPersistable<Integer> implements BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;
/*
    @ManyToMany(mappedBy = "theme_classes")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private Set<OrganizationEntity> organizationEntities;*/

    @Override
    public BaseRepresentationObject getRepresentation() {
        ThemeClassDTO themeClass = new ThemeClassDTO(getId(), getName());
        return themeClass;
    }
}
