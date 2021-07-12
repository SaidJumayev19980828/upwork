package com.nasnav.persistence;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "integration_mapping_type")
@Data
public class IntegrationMappingTypeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="type_name")
	private String typeName;	
}
