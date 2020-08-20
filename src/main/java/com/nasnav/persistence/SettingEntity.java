package com.nasnav.persistence;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name="settings")
public class SettingEntity {
	
	@Id
    @GeneratedValue(strategy=IDENTITY)
	private Long id;
	
	@Column(name="setting_name")
	private String settingName;
	
	@Column(name="setting_value")
	private String settingValue;

	@ManyToOne
	@JoinColumn(name = "organization_id")
	private OrganizationEntity organization;
}
