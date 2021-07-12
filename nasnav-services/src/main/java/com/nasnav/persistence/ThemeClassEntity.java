package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.ThemeClassDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.Set;

import static javax.persistence.FetchType.LAZY;

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

    @OneToMany(mappedBy = "themeClassEntity", fetch = LAZY)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private Set<ThemeEntity> themes;
/*
    @ManyToMany(mappedBy = "theme_classes")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private Set<OrganizationEntity> organizationEntities;*/

    @Override
    public ThemeClassDTO getRepresentation() {
        ThemeClassDTO themeClass = new ThemeClassDTO(getId(), getName());
        return themeClass;
    }
}
