package com.nasnav.persistence;

import com.nasnav.dto.request.LoyaltyCoinsDropLogsDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;

@Entity
@Table(name="loyalty_coins_drop_logs")
@Data
public class LoyaltyCoinsDropLogsEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private OrganizationEntity organization;

    @ManyToOne
    @JoinColumn(name = "coins_drop_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private LoyaltyCoinsDropEntity coinsDrop;

    @ManyToOne
    @JoinColumn(name = "created_by")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity user;

    @Column(name="is_active")
    private Boolean isActive;

    public LoyaltyCoinsDropLogsDTO getRepresentation() {
        LoyaltyCoinsDropLogsDTO dto = new LoyaltyCoinsDropLogsDTO();
        BeanUtils.copyProperties(this, dto);
        dto.setOrgId(organization.getId());
        dto.setUserId(user.getId());
        return dto;
    }
}
