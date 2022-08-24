package com.nasnav.persistence.yeshtery;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.enumerations.UserStatus;
import com.nasnav.persistence.AddressesEntity;
import com.nasnav.persistence.yeshtery.listeners.YeshteryUserEntityListener;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static com.nasnav.enumerations.UserStatus.NOT_ACTIVATED;

@Data
@Entity
@EntityListeners(YeshteryUserEntityListener.class)
@Table(name = "yeshtery_users")
@EqualsAndHashCode(callSuper=false)
public class YeshteryUserEntity extends BaseYeshteryUserEntity {

    @Column(name="user_name")
    private String name;

    @Column(name="first_name")
    private String firstName;

    @Column(name="last_name")
    private String lastName;

    @Column(name="image")
    private String image;

    @Column(name="mobile")
    private String mobile;

    @Column(name = "remember_created_at")
    @CreationTimestamp
    private LocalDateTime creationTime;

    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JoinTable(name = "yeshtery_user_addresses"
            ,joinColumns = {@JoinColumn(name="yeshtery_user_id")}
            ,inverseJoinColumns = {@JoinColumn(name="address_id")})
    private Set<AddressesEntity> addresses;

    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<YeshteryUserAddressEntity> userAddresses;


    private String referral;


    public YeshteryUserEntity() {
    	this.setUserStatus(NOT_ACTIVATED.getValue());
    	addresses = new HashSet<>();
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
        obj.setCreationDate(creationTime);
        obj.setStatus(UserStatus.getUserStatus(getUserStatus()).name());

        return obj;
    }
}
