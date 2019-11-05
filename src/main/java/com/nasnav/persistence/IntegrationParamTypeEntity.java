package com.nasnav.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

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
