package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.request.LoyaltyPointDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "loyalty_points")
@EqualsAndHashCode(callSuper=false)
public class LoyaltyPointEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;
    private BigDecimal amount;
    private BigDecimal points;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private OrganizationEntity organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private LoyaltyPointTypeEntity type;

    public LoyaltyPointDTO getRepresentation() {
        LoyaltyPointDTO dto = new LoyaltyPointDTO();
        BeanUtils.copyProperties(this, dto);
        dto.setOrgId(this.getOrganization().getId());
        dto.setTypeId(this.getType().getId());
        dto.setType(this.getType().getName());
        return dto;
    }
}
