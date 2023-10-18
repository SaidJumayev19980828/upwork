package com.nasnav.persistence;

import com.nasnav.dto.request.LoyaltyTierDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "loyalty_tier")
@EqualsAndHashCode(callSuper=false)
public class LoyaltyTierEntity {

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

    @Column( name = "cash_back_percentage")
    private BigDecimal cashBackPercentage;

    @Column(name = "constraints")
    private String constraints;


    public LoyaltyTierDTO getRepresentation() {
        LoyaltyTierDTO dto = new LoyaltyTierDTO();
        BeanUtils.copyProperties(this, dto);
        if(organization != null ) {
            dto.setOrgId(organization.getId());
        }
        return dto;
    }
}
