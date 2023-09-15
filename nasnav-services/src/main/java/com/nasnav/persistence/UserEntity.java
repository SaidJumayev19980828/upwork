package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.constatnts.EntityConstants;
import com.nasnav.dto.UserDTOs;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.enumerations.UserStatus;
import com.nasnav.persistence.yeshtery.listeners.UserEntityListener;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static com.nasnav.enumerations.UserStatus.NOT_ACTIVATED;

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

    @Column(name="mobile")
    private String mobile;


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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", referencedColumnName = "id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private LoyaltyFamilyEntity family;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tier_id", referencedColumnName = "id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private LoyaltyTierEntity tier;

    @Column(name = "allow_reward")
    private Boolean allowReward;

    @Column(name = "tier_created_at")
    private LocalDateTime tierCreatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booster_id", referencedColumnName = "id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private LoyaltyBoosterEntity booster;

    @Column(name = "booster_created")
    private LocalDateTime boosterCreated;

    @Column(name = "yeshtery_user_id")
    private Long yeshteryUserId;

    private String referral;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    @JsonIgnore
    RocketChatCustomerTokenEntity rocketChatTokenEntity;

    public void insertUserAddress(AddressesEntity address) {this.addresses.add(address);}

    public void removeUserAddress(AddressesEntity address) {this.addresses.remove(address);}

    public UserEntity() {
    	this.setUserStatus(NOT_ACTIVATED.getValue());
    	addresses = new HashSet<>();
        allowReward = false;
    }

    
    public static UserEntity registerUser(UserDTOs.UserRegistrationObject userJson) {
    	UserEntity user = new UserEntity();
        user.setName(userJson.getName());
        user.setEmail(userJson.getEmail());
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
        obj.setId(getId());
        if (this.getFamily() != null)
            obj.setFamilyId(this.getFamily().getId());
        if (this.getTier() != null)
            obj.setTierId(this.getTier().getId());
        obj.setAllowReward(getAllowReward());
        obj.setCreationDate(getCreationTime());
        obj.setTierCreatedAt(this.getTierCreatedAt());
        if (this.getBooster() != null)
            obj.setBoosterId(this.booster.getId());
        obj.setStatus(UserStatus.getUserStatus(getUserStatus()).name());
        obj.setImage(this.getImage());

        return obj;
    }
}
