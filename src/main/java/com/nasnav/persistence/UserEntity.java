package com.nasnav.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;

import com.nasnav.constatnts.EntityConstants;
import com.nasnav.dto.Address;
import com.nasnav.dto.UserDTOs;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.persistence.listeners.UserEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@EntityListeners(UserEntityListener.class)
@Table(name = "users")
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class UserEntity extends BaseUserEntity{

    @Column(name="user_name")
    private String name;


    @Column(name="address")
    private String address;

    @Column(name="country")
    private String addressCountry;

    @Column(name="city")
    private String addressCity;

    @Column(name="image")
    private String image;

    
    public static UserEntity registerUser(UserDTOs.UserRegistrationObject userJson) {
    	UserEntity user = new UserEntity();
        user.setName(userJson.name);
        user.setEmail(userJson.email);
        user.setEncryptedPassword(EntityConstants.INITIAL_PASSWORD);
        user.setOrganizationId(userJson.getOrgId());
        return user;
    }
    
    
    @Override
    public UserRepresentationObject getRepresentation() {
        UserRepresentationObject obj = new UserRepresentationObject();
        obj.id = this.getId();
        obj.name = this.getName();
        obj.email = this.getEmail();
        obj.phoneNumber = this.getPhoneNumber();
        obj.image = this.getImage();
        //TODO set mobile after including in in DB
        Address address = new Address();
        address.setCountry(this.getAddressCountry());
        address.setCity(this.getAddressCity());
        address.setStreet(this.getAddress());
        obj.address = address;
        return obj;
    }
}
