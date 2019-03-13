package com.nasnav.persistence;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.net.InetAddress;
import java.util.Date;

@Entity
@Table(name = "users")
public class UserEntity extends AbstractPersistable<Long> {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name="email", unique = true)
    public String email;

    @Column(name="user_name", unique = true)
    public String name;

    @Column(name="encrypted_password")
    public String encPassword;

    @Column(name="sign_in_count")
    public int signInCount;

    @Column(name="last_sign_in_at")
    public Date lastSignInDate;

    @Column(name="current_sign_in_at")
    public Date currentSignInDate;

    @Column(name="authentication_token")
    public String authenticationToken;

    @Column(name="address")
    public String address;

    @Column(name="country")
    public String addressCountry;

    @Column(name="city")
    public String addressCity;

    @Column(name="created_at")
    public Date createdAt;

    @Column(name="updated_at")
    public Date updatedAt;

    @Column(name="phone_number")
    public String phoneNumber;

    public UserEntity() {
        this.id = null;
    }

    public Long getId() {
        return this.id;
    }

}
