package com.nasnav.persistence;

import lombok.Data;

import javax.persistence.*;

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
