package com.nasnav.persistence;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.nasnav.constatnts.EntityConstants;
import com.nasnav.dto.UserDTOs;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class UserEntity extends BaseUserEntity{
	
	@Column(name = "user_name")
	private String name;
	
    @Column(name="address")
    private String address;

    @Column(name="country")
    private String addressCountry;

    @Column(name="city")
    private String addressCity;
    
    
    
    
    public static UserEntity registerUser(UserDTOs.UserRegistrationObject userJson) {
    	UserEntity user = new UserEntity();
        user.setName(userJson.name);
        user.setEmail(userJson.email);
        user.setEncryptedPassword(EntityConstants.INITIAL_PASSWORD);
        user.setOrganizationId(userJson.org_id);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

}
