package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.Set;


@Data
@Entity
@Table(name = "roles")
@EqualsAndHashCode(callSuper=false)
public class Role extends DefaultBusinessEntity<Integer>{

    @Column(name = "name")
    private String name;

    @Column(name = "organization_id")
    private Long organizationId;

    @ManyToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JoinTable(name = "role_employee_users"
            ,joinColumns = {@JoinColumn(name="role_id")}
            ,inverseJoinColumns = {@JoinColumn(name="employee_user_id")})
    private Set<EmployeeUserEntity> employees;

    @ManyToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JoinTable(name = "role_permissions"
            ,joinColumns = {@JoinColumn(name="role_id")}
            ,inverseJoinColumns = {@JoinColumn(name="permission_id")})
    private Set<Permission> permissions;

    public Role() {
    }

    public Role(String name, Long organizationId) {
        this.name = name;
        this.organizationId = organizationId;
    }
}
