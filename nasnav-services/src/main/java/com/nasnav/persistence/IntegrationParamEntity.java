package com.nasnav.persistence;

import lombok.Data;

import javax.persistence.*;
import java.util.Optional;

@Entity
@Table(name = "integration_param")
@Data
public class IntegrationParamEntity {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name="param_type")
	private IntegrationParamTypeEntity type;
	
	@Column(name= "organization_id")
	private Long organizationId;
	
	@Column(name= "param_value")
	private String paramValue;
	
	
	
	public String getParameterTypeName() {
		return Optional.ofNullable(type)
						.map(IntegrationParamTypeEntity::getTypeName)
						.orElse(null);
	}
}
