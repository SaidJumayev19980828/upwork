package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "loyalty_family")
@EqualsAndHashCode(callSuper=false)
public class LoyaltyFamilyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "family_name")
    private String familyName;

    @OneToOne
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private UserEntity parent;

    @OneToOne
    @JoinColumn(name = "booster_id", referencedColumnName = "id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private LoyaltyBoosterEntity booster;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private OrganizationEntity organization;
}
