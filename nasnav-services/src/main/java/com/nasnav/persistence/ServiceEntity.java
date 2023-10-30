package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

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

    @ManyToMany(fetch = FetchType.LAZY)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    @JoinTable(name = "package_service"
            ,joinColumns = {@JoinColumn(name="service_id")}
            ,inverseJoinColumns = {@JoinColumn(name="package_id")})
    private Set<PackageEntity> packageEntity = new HashSet<>();

}
