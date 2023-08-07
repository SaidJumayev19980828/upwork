package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.enumerations.UserStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
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

    @Column(name = "phone_number")
    private String phoneNumber;

    @ManyToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JoinTable(name = "role_employee_users"
            ,joinColumns = {@JoinColumn(name="employee_user_id")}
            ,inverseJoinColumns = {@JoinColumn(name="role_id")})
    private Set<Role> roles;

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
        obj.setImage(this.getImage());
        return obj;
    }
}
