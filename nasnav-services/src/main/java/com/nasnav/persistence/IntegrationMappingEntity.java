package com.nasnav.persistence;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Optional;

@Entity
@Table(name = "integration_mapping")
@Data
@NoArgsConstructor
public class IntegrationMappingEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name="mapping_type")
	private IntegrationMappingTypeEntity mappingType;
	
	@Column(name="local_value")
	private String localValue;
	
	
	@Column(name="remote_value")
	private String remoteValue;
	
	
	@Column(name="organization_id")
	private Long organizationId;
	
	
	
	
	public IntegrationMappingEntity(Long organizationId, IntegrationMappingTypeEntity mappingType, String localValue, String remoteValue) {
		this.organizationId = organizationId;
		this.mappingType = mappingType;
		this.localValue = localValue;
		this.remoteValue = remoteValue;
	}
	
	
	
	
	public String getMappingTypeName() {
		return Optional.ofNullable(mappingType)
						.map(IntegrationMappingTypeEntity::getTypeName)
						.orElse(null);
	}
}
