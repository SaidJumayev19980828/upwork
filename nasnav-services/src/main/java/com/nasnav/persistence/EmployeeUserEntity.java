package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.enumerations.UserStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;

@Data
@Entity
@Table(name = "employee_users")
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class EmployeeUserEntity extends BaseUserEntity {
	
	@Column(name = "name")
	private String name;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "shop_id")
    private Long shopId;

    @Column(name = "organization_manager_id")
    private Long organizationManagerId;

    @ManyToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JoinTable(name = "role_employee_users"
            ,joinColumns = {@JoinColumn(name="employee_user_id")}
            ,inverseJoinColumns = {@JoinColumn(name="role_id")})
    private Set<Role> roles;

    @OneToOne(mappedBy = "employeeUser", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    @JsonIgnore
    private InfluencerEntity influencer;

    @Override
    public UserRepresentationObject getRepresentation() {
        UserRepresentationObject obj = new UserRepresentationObject();
        BeanUtils.copyProperties(this, obj);
        obj.setId(getId());
        obj.setCreationDate(getCreationTime());
        obj.setStatus(UserStatus.getUserStatus(getUserStatus()).name());
        obj.setImage(getImage());
        obj.setRoles(
                ofNullable(roles)
                .orElse(emptySet())
                .stream()
                .map(Role::getName)
                .collect(toSet())
        );
        if(this.influencer != null) {
            obj.setInfluencerId(this.influencer.getId());
            obj.setIsGuided(this.influencer.getIsGuided());
            obj.setIsInfluencer(this.influencer.getApproved());
        }
        return obj;
    }

    /**
     * Retrieves the shop ID associated with the employee user. If the shop ID is null, it returns 0L.
     *
     * @return the shop ID of the employee user, or 0L if the shop ID is null
     */
    @Override
    public Long getShopId() {
        return  ObjectUtils.firstNonNull(this.shopId, 0L);
    }

    @Override
    public boolean isEmployee() {
        return true;
    }
}
