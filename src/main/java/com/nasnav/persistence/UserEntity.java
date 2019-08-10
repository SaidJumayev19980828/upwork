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
public class UserEntity extends DefaultBusinessEntity<Long> {

    @Column(name="email")
    private String email;

    @Column(name="user_name")
    private String name;
    
    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name="encrypted_password")
    private String encPassword;

    @Column(name="sign_in_count")
    private int signInCount;

    @Column(name="last_sign_in_at")
    private LocalDateTime lastSignInDate;

    @Column(name="current_sign_in_at")
    private LocalDateTime currentSignInDate;

    @Column(name="authentication_token")
    private String authenticationToken;

    @Column(name="address")
    private String address;

    @Column(name="country")
    private String addressCountry;

    @Column(name="city")
    private String addressCity;

    @Column(name="phone_number")
    private String phoneNumber;

    @Column(name="reset_password_token")
    private String resetPasswordToken;

    @Column(name="reset_password_sent_at")
    private LocalDateTime resetPasswordSentAt;

    @Column(name = "gender")
    private String gender;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "birth_date")
    private String birthDate;

    @Column(name = "post_code")
    private String postCode;

    @Column(name = "flat_number")
    private Integer flatNumber;

    @Column(name = "image")
    private String image;

    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
    
    public static UserEntity registerUser(UserDTOs.UserRegistrationObject userJson) {
    	UserEntity user = new UserEntity();
        user.setName(userJson.name);
        user.setEmail(userJson.email);
        user.setEncryptedPassword(EntityConstants.INITIAL_PASSWORD);
        user.setOrganizationId(userJson.getOrgId());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

}
