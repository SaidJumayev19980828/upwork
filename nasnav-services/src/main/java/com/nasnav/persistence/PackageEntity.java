package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;

@Table(name = "package")
@Entity
@Data
public class PackageEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    private Set<PackagesRegisteredEntity> packagesRegistered;

    @OneToMany(mappedBy = "packageEntity")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private Set<PackageRegisteredEntity> packageRegistered;

}
