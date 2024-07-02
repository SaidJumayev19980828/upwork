package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.math.BigDecimal;
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
    private BigDecimal price = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_iso", referencedColumnName = "iso_code")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private CountriesEntity country;

    //Period In Days
    @Column(name = "period_in_days")
    private Long periodInDays = 0l;

    @Column(name="stripe_price_id")
    private String stripePriceId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "package_service"
            ,joinColumns = {@JoinColumn(name="package_id")}
            ,inverseJoinColumns = {@JoinColumn(name="service_id")})
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private Set<ServiceEntity> services;
}
