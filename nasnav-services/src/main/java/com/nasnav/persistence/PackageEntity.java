package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Table(name = "package")
@EqualsAndHashCode(callSuper=false)
@Entity
@Data
public class PackageEntity extends DefaultBusinessEntity<Long> {
    @Column(name="name")
    private String name;

    @Column(name="description")
    private String description;

    @Column(name = "price")
    private BigDecimal price;

    @OneToMany(mappedBy = "packageEntity")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private Set<ServicesRegisteredInPackage> servicesIncluded = new HashSet<>();

    @OneToMany(mappedBy = "packageEntity", fetch = FetchType.LAZY)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private Set<PackageRegisteredEntity> packageRegistered = new HashSet<>();
}
