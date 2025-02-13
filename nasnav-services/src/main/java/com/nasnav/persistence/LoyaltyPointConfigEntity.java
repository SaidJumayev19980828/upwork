package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.request.LoyaltyPointConfigDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "loyalty_point_config")
@EqualsAndHashCode(callSuper=false)
public class LoyaltyPointConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private OrganizationEntity organization;

    @Column(name = "constraints")
    private String constraints;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_tier_id", referencedColumnName = "id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private LoyaltyTierEntity defaultTier;

    public LoyaltyPointConfigDTO getRepresentation() {
        LoyaltyPointConfigDTO dto = new LoyaltyPointConfigDTO();
        BeanUtils.copyProperties(this, dto);

        if(organization != null)
            dto.setOrgId(organization.getId());

        if(defaultTier != null) {
            dto.setDefaultTier(defaultTier.getRepresentation());
        }
        return dto;
    }
}
