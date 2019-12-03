package com.nasnav.persistence;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Data;
import lombok.NoArgsConstructor;

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
	
	
	@Column(name="created_at")
	@CreationTimestamp
	private LocalDateTime createdAt;
	
	
	@Column(name="updated_at")
	@UpdateTimestamp
	private LocalDateTime updatedAt;
	
	
	
	
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
