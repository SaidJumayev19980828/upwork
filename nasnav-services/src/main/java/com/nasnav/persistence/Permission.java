package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.Set;

@Data
@Entity
@Table(name = "permission")
@EqualsAndHashCode(callSuper=false)
public class Permission extends DefaultBusinessEntity<Long> {

    @Column(name = "name")
    private String name;

    @ManyToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JoinTable(name = "role_permissions"
            ,joinColumns = {@JoinColumn(name="permission_id")}
            ,inverseJoinColumns = {@JoinColumn(name="role_id")})
    private Set<Role> roles;

    @ManyToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JoinTable(name = "service_permissions"
            ,joinColumns = {@JoinColumn(name="permission_id")}
            ,inverseJoinColumns = {@JoinColumn(name="service_id")})
    private Set<ServiceEntity> services;


    public Permission() {
    }

    public Permission(String name) {
        this.name = name;
    }

    public Permission(Long id, String name) {
        super.setId(id);
        this.name = name;
    }
}
