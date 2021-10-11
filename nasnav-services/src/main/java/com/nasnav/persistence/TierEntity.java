package com.nasnav.persistence;

import com.nasnav.dto.request.TierDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tier")
@EqualsAndHashCode(callSuper=false)
public class TierEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tier_name")
    private String tierName;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "is_special")
    private Boolean isSpecial;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "no_of_purchase_from")
    private Integer noOfPurchaseFrom;

    @Column(name = "no_of_purchase_to")
    private Integer noOfPurchaseTo;

    @Column(name = "selling_price")
    private Integer sellingPrice;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private OrganizationEntity organization;

    @OneToOne
    @JoinColumn(name = "booster_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private BoosterEntity booster;

    public TierDTO getRepresentation() {
        TierDTO dto = new TierDTO();
        BeanUtils.copyProperties(this, dto);
        dto.setOrgId(organization.getId());
        dto.setBoosterId(booster.getId());
        return dto;
    }
}
