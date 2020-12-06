package com.nasnav.persistence;

import static com.nasnav.enumerations.UserStatus.NOT_ACTIVATED;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.constatnts.EntityConstants;
import com.nasnav.dto.UserDTOs;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.persistence.listeners.UserEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@EntityListeners(UserEntityListener.class)
@Table(name = "users")
@EqualsAndHashCode(callSuper=false)
public class UserEntity extends BaseUserEntity{

    @Column(name="user_name")
    private String name;

    @Column(name="first_name")
    private String firstName;

    @Column(name="last_name")
    private String lastName;

    @Column(name="image")
    private String image;

    @Column(name="user_status")
    private Integer userStatus;

    @Column(name="mobile")
    private String mobile;

    @Column(name = "remember_created_at")
    @CreationTimestamp
    private LocalDateTime creationTime;

    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JoinTable(name = "user_addresses"
            ,joinColumns = {@JoinColumn(name="user_id")}
            ,inverseJoinColumns = {@JoinColumn(name="address_id")})
    private Set<AddressesEntity> addresses;

    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<UserAddressEntity> userAddresses;

    public void insertUserAddress(AddressesEntity address) {this.addresses.add(address);}


    public void removeUserAddress(AddressesEntity address) {this.addresses.remove(address);}

    public UserEntity() {
    	userStatus = NOT_ACTIVATED.getValue();
    	addresses = new HashSet<>();
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
        BeanUtils.copyProperties(this, obj);
        obj.id = this.getId();
        obj.name = this.getName();
        obj.email = this.getEmail();
        obj.phoneNumber = this.getPhoneNumber();
        obj.image = this.getImage();
        obj.mobile = this.getMobile();

        return obj;
    }
}
