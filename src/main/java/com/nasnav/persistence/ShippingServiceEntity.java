package com.nasnav.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name="shipping_service")
@Data
public class ShippingServiceEntity {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private String id;
	
	@Column(name="service_parameters")
	private String serviceParameters;
	
	@Column(name="additional_parameters")
	private String addtionalParameters;
}
