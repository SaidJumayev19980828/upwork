package com.nasnav.persistence;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
@EqualsAndHashCode(callSuper=false)
public class UserEntity extends DefaultBusinessEntity<Long> {

    @Column(name="email", unique = true)
    private String email;

    @Column(name="user_name", unique = true)
    private String name;

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

    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    public UserEntity() {
        super();
    }

}
