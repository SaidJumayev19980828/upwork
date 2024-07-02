package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Table(name = "service")
@EqualsAndHashCode(callSuper=false)
@Entity
@Data
public class ServiceEntity extends DefaultBusinessEntity<Long> {

    @Column(name="code" , unique = true)
    private String code;

    @Column(name="name")
    private String name;

    @Column(name="description")
    private String description;

    @Column(name="light_logo")
    private String lightLogo;

    @Column(name="dark_logo")
    private String darkLogo;

    @Column(name="enabled")
    private Boolean enabled;

    @ManyToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinTable(name = "package_service"
            ,joinColumns = {@JoinColumn(name="service_id")}
            ,inverseJoinColumns = {@JoinColumn(name="package_id")})
    private Set<PackageEntity> packageEntity = new HashSet<>();


    @ManyToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JoinTable(name = "service_permissions"
            ,joinColumns = {@JoinColumn(name="service_id")}
            ,inverseJoinColumns = {@JoinColumn(name="permission_id")})
    private Set<Permission> permissions;
}
