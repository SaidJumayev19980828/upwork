package com.nasnav.persistence;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Data;
import lombok.EqualsAndHashCode;

@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper=false)
public class BaseUserEntity extends DefaultBusinessEntity<Long>{
	
	@Column(name = "name")
	private String name;

	@Column(name = "created_at")
	@CreationTimestamp
	private LocalDateTime createdAt;
	
	@Column(name = "updated_at")
	@UpdateTimestamp
	private LocalDateTime updatedAt;
	
	@Column(name = "email")
	private String email;
	
	@Column(name = "encrypted_password")
	private String encryptedPassword;
	
	@Column(name = "reset_password_token")
	private String resetPasswordToken;
	
	@Column(name = "reset_password_sent_at")
	private LocalDateTime resetPasswordSentAt;
	
	@Column(name = "avatar")
	private String avatar;
	
	@Column(name = "organization_id")
	private Long organizationId;
	
	@Column(name = "authentication_token")
	private String authenticationToken;

	@Column(name = "last_sign_in_at")
	private LocalDateTime lastSignInDate;

	@Column(name = "current_sign_in_at")
	private LocalDateTime currentSignInDate;

	@Column(name = "phone_number")
	private String phoneNumber;

	@Column(name = "sign_in_count")
	private int signInCount;
	
}
