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

@Data
@Entity
@Table(name="OAUTH2_USERS")
public class OAuth2UserEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name =" OAUTH2_ID")
	private String oAuth2Id;
	
	@Column(name = "email")
	private String email;
	
	@Column(name = "ORGANIZATION_ID")
	private Long organizationId;
	
	@Column(name = "LOGIN_TOKEN")
	private String loginToken;
	
	@ManyToOne
	@JoinColumn(name="NASNAV_USER_ID")
	UserEntity user;
	
	@ManyToOne
	@JoinColumn(name="provider_id")
	OAuth2ProviderEntity provider;
}
