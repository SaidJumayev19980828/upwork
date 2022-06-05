package com.nasnav.persistence;

import com.nasnav.dto.request.LoyaltyCharityDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "loyalty_charity")
@Data
public class LoyaltyCharityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="charity_name")
    private String charityName;

    @Column(name = "total_donation")
    private Integer totalDonation;

    @Column(name="is_active")
    private Boolean isActive;

    @Column(name="created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private OrganizationEntity organization;

    public LoyaltyCharityDTO getRepresentation() {
        LoyaltyCharityDTO dto = new LoyaltyCharityDTO();
        BeanUtils.copyProperties(this, dto);
        dto.setOrgId(organization.getId());
        return dto;
    }

}
