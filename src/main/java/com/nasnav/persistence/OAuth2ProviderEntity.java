package com.nasnav.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name="OAUTH2_PROVIDERS")
@NoArgsConstructor
public class OAuth2ProviderEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="PROVIDER_NAME")
	private String providerName;
	
	
	public OAuth2ProviderEntity(String providerName) {
		this.providerName = providerName;
	}
	
}
