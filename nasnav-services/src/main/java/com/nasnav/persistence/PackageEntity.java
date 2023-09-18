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
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "period" , nullable = false)
    private Long period;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "package_service"
            ,joinColumns = {@JoinColumn(name="package_id")}
            ,inverseJoinColumns = {@JoinColumn(name="service_id")})
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private Set<ServiceEntity> services;

}
