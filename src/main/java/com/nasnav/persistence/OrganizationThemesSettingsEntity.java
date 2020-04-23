package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.OrganizationThemesSettingsDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;

@Table(name = "organization_themes_settings")
@Entity
@Data
@EqualsAndHashCode(callSuper=false)
public class OrganizationThemesSettingsEntity extends AbstractPersistable<Integer> implements BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    @JsonIgnore
    private OrganizationEntity organizationEntity;

    @Column(name = "theme_id")
    private Integer themeId;

    @Column(name = "settings")
    private String settings;

    @Override
    public BaseRepresentationObject getRepresentation() {
        OrganizationThemesSettingsDTO themesDTO = new OrganizationThemesSettingsDTO();

        themesDTO.setId(getId());
        themesDTO.setSettings(getSettings());
        themesDTO.setThemeId(getThemeId());

        return themesDTO;
    }
}
