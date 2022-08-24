package com.nasnav.persistence;

import com.nasnav.dto.UserRepresentationObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Column;
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

	abstract public String getName();
	abstract public void setName(String name);

	abstract public UserRepresentationObject getRepresentation();
	
}
