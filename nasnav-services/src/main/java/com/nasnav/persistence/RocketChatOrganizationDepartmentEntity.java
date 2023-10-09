package com.nasnav.persistence;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "rocket_chat_organization_departments")
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class RocketChatOrganizationDepartmentEntity {
	@Id
	private Long orgId;

	private String departmentId;

	@MapsId
	@OneToOne(optional = false)
	@JoinColumn(name = "org_id", referencedColumnName = "id")
	private OrganizationEntity organization;
}