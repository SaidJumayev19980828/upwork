package com.nasnav.persistence;

import java.time.LocalDateTime;

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
	
	@CreationTimestamp
	@Column(name= "created_at")
	private LocalDateTime createdAt;
	
	
	@UpdateTimestamp
	@Column(name= "updated_at")
	private LocalDateTime updatedAt;
}
