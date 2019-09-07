package com.nasnav.persistence;

import com.nasnav.dto.Address;
import com.nasnav.dto.UserRepresentationObject;
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
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    public static UserRepresentationObject getRepresentation(UserEntity userEntity) {
        UserRepresentationObject obj = new UserRepresentationObject();
        obj.id = userEntity.getId();
        obj.name = userEntity.getName();
        obj.email = userEntity.getEmail();
        obj.phoneNumber = userEntity.getPhoneNumber();
        obj.image = userEntity.getImage();
        //TODO set mobile, image after including in in DB
        Address address = new Address();
        address.setCountry(userEntity.getAddressCountry());
        address.setCity(userEntity.getAddressCity());
        address.setStreet(userEntity.getAddress());
        obj.address = address;
        return obj;
    }
}
