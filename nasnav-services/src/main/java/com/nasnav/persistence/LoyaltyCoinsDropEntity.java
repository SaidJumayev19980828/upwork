package com.nasnav.persistence;

import com.nasnav.dto.request.LoyaltyCoinsDropDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="loyalty_coins_drop")
@Data
public class LoyaltyCoinsDropEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private OrganizationEntity organization;

    @Column(name="type_id")
    private Integer typeId;

    @Column(name="created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name="amount")
    private BigDecimal amount;

    @Column(name = "official_vacation_date")
    private LocalDate officialVacationDate;

    @Column(name="is_active")
    private Boolean isActive;

    public LoyaltyCoinsDropDTO getRepresentation() {
        LoyaltyCoinsDropDTO dto = new LoyaltyCoinsDropDTO();
        BeanUtils.copyProperties(this, dto);
        dto.setOrgId(organization.getId());
        return dto;
    }
}
