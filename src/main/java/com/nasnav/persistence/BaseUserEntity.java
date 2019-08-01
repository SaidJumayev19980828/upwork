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
public abstract class BaseUserEntity {	

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
	
	
	//TODO: this is a work around because the column type of employee_users.id is int4 which is not converted into Long
    //by hibernate.
	//So, we can't make id as a common property for all subclasses , instead we leave it to each subclass to implement
	//its id property 
	abstract Long getId();
	abstract void setId(Long id);
	
}
