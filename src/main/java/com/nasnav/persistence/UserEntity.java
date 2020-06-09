package com.nasnav.persistence;

import static com.nasnav.enumerations.UserStatus.NOT_ACTIVATED;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.constatnts.EntityConstants;
import com.nasnav.dto.AddressRepObj;
import com.nasnav.dto.UserDTOs;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.persistence.listeners.UserEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@Entity
@EntityListeners(UserEntityListener.class)
@Table(name = "users")
@EqualsAndHashCode(callSuper=false)
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

    @Column(name="user_status")
    private Integer userStatus;

    @Column(name="mobile")
    private String mobile;

    @OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JoinTable(name = "user_addresses"
            ,joinColumns = {@JoinColumn(name="user_id")}
            ,inverseJoinColumns = {@JoinColumn(name="address_id")})
    private Set<AddressesEntity> addresses;



    public UserEntity() {
    	userStatus = NOT_ACTIVATED.getValue();
    }

    
    public static UserEntity registerUser(UserDTOs.UserRegistrationObject userJson) {
    	UserEntity user = new UserEntity();
        user.setName(userJson.name);
        user.setEmail(userJson.email);
        user.setEncryptedPassword(EntityConstants.INITIAL_PASSWORD);
        user.setOrganizationId(userJson.getOrgId());
        user.setUserStatus(NOT_ACTIVATED.getValue());
        user.setPhoneNumber(userJson.getPhoneNumber());
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
        obj.mobile = this.getMobile();

        Set<AddressRepObj> userAddresses = this.getAddresses().stream().map(a-> (AddressRepObj)a.getRepresentation()).collect(Collectors.toSet());;

        obj.addresses = userAddresses;

        return obj;
    }
}
