package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.enumerations.Gender;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Formula;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper=false)
public abstract class BaseUserEntity extends DefaultBusinessEntity<Long>{
	
	@Column(name = "email")
	private String email;
	
	@Column(name = "encrypted_password")
	private String encryptedPassword;
	
	@Column(name = "reset_password_token")
	private String resetPasswordToken;
	
	@Column(name = "reset_password_sent_at")
	private LocalDateTime resetPasswordSentAt;
	
	@Column(name = "avatar")
	private String image;
	
	@Column(name = "organization_id")
	private Long organizationId;

	@Enumerated(EnumType.STRING)
	@Column(name = "gender")
	private Gender gender;

	@Deprecated
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

	@Column(name="user_status")
	private Integer userStatus;

	@Column(name = "remember_created_at")
	@CreationTimestamp
	private LocalDateTime creationTime;

	@Column(name = "date_of_birth")
	private LocalDateTime dateOfBirth;

	public abstract String getName();
	public abstract void setName(String name);

	public abstract UserRepresentationObject getRepresentation();
	
}
