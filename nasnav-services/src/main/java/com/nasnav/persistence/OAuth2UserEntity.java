package com.nasnav.persistence;

import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;

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
	@Fetch(FetchMode.JOIN) 
	private UserEntity user;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="provider_id")
	@Fetch(FetchMode.JOIN) 
	private OAuth2ProviderEntity provider;
}
