package com.nasnav.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name="organiztion_cart_optimization")
@Data
public class OrganizationCartOptimization {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@Column(name="optimization_strategy")
	private String optimizationStrategy;
	
	@ManyToOne
	@JoinColumn(name="organization_id", referencedColumnName = "id")
	private OrganizationEntity organization;
	
	@Column(name="optimization_parameters")
	private String parameters;
	
	@Column(name="shipping_service_id")
	private String shippingServiceId;
}
