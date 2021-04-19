package com.nasnav.persistence;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

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
