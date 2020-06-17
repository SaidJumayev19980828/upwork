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
@Table(name="organization_shipping_service")
@Data
public class OrganizationShippingServiceEntity {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@ManyToOne
	@JoinColumn(name="shipping_service_id", referencedColumnName = "id")
	private ShippingServiceEntity service;
	
	@ManyToOne
	@JoinColumn(name="organization_id", referencedColumnName = "id")
	private OrganizationEntity organization;
	
	@Column(name="service_parameters")
	private String serviceParameters;
}
