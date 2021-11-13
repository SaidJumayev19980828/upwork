package com.nasnav.persistence;

import com.nasnav.dto.request.LoyaltyBoosterDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;

@Data
@Entity
@Table(name = "loyalty_booster")
@EqualsAndHashCode(callSuper = false)
public class LoyaltyBoosterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booster_name")
    private String boosterName;

    @Column(name = "linked_family_member")
    private Integer linkedFamilyMember;

    @Column(name = "number_family_children")
    private Integer numberFamilyChildren;

    @Column(name = "purchase_size")
    private Integer purchaseSize;

    @Column(name = "review_products")
    private Integer reviewProducts;

    @Column(name = "number_purchase_offline")
    private Integer numberPurchaseOffline;

    @Column(name = "social_media_reviews")
    private Integer socialMediaReviews;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private OrganizationEntity organization;

    @Column(name = "level_booster")
    private Integer levelBooster;

    @Column(name = "activation_months")
    private Integer activationMonths;

    @Column(name = "is_active")
    private Boolean isActive;

    public LoyaltyBoosterDTO getRepresentation() {
        LoyaltyBoosterDTO dto = new LoyaltyBoosterDTO();
        BeanUtils.copyProperties(this, dto);
        dto.setOrgId(organization.getId());
        return dto;
    }

}
