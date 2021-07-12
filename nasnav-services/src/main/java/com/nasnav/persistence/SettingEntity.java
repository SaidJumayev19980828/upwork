package com.nasnav.persistence;

import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.request.organization.SettingDTO;
import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@Data
@Entity
@Table(name="settings")
public class SettingEntity implements BaseEntity{
	
	@Id
    @GeneratedValue(strategy=IDENTITY)
	private Long id;
	
	@Column(name="setting_name")
	private String settingName;
	
	@Column(name="setting_value")
	private String settingValue;

	private Integer type;

	@ManyToOne
	@JoinColumn(name = "organization_id")
	private OrganizationEntity organization;

	@Override
	public BaseRepresentationObject getRepresentation() {
		SettingDTO dto = new SettingDTO();
		dto.setName(settingName);
		dto.setValue(settingValue);
		dto.setType(type);
		return dto;
	}
}
