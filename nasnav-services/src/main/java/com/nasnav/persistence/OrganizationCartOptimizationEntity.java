package com.nasnav.persistence;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name="organiztion_cart_optimization")
@Data
public class OrganizationCartOptimizationEntity {
	
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
