package com.nasnav.persistence;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "integration_param_type")
@Data
public class IntegrationParamTypeEntity {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;
	
	
	@Column(name= "type_name")
	String typeName;
	
	@Column(name= "is_mandatory")
	boolean isMandatory;
}
