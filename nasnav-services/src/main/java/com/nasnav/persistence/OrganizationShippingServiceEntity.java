package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name="organization_shipping_service")
@Data
public class OrganizationShippingServiceEntity {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	
	@Column(name="shipping_service_id")
	private String serviceId;
	
	@ManyToOne(fetch = LAZY)
	@JoinColumn(name="organization_id", referencedColumnName = "id")
	@JsonIgnore
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	private OrganizationEntity organization;
	
	@Column(name="service_parameters")
	private String serviceParameters;
}
