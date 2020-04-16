package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.ThemeDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;

@Table(name = "themes")
@Entity
@Data
@EqualsAndHashCode(callSuper=false)
public class ThemeEntity extends AbstractPersistable<Integer> implements BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "image")
    private String previewImage;

    @Column(name = "default_settings")
    private String defaultSettings;

    @ManyToOne
    @JoinColumn(name = "theme_class_id", referencedColumnName = "id")
    @JsonIgnore
    private ThemeClassEntity themeClassEntity;


    @Override
    public BaseRepresentationObject getRepresentation() {
        ThemeDTO themeDTO = new ThemeDTO();

        themeDTO.setId(getId());
        themeDTO.setName(getName());
        themeDTO.setPreviewImage(getPreviewImage());
        themeDTO.setDefaultSettings(getDefaultSettings());
        if(getThemeClassEntity() != null)
            themeDTO.setThemeClassId(getThemeClassEntity().getId());

        return themeDTO;
    }
}
