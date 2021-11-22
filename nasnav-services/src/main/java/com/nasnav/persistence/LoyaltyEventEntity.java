package com.nasnav.persistence;

import com.nasnav.dto.request.LoyaltyEventDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "loyalty_events")
@EqualsAndHashCode(callSuper=false)
public class LoyaltyEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private OrganizationEntity organization;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    public LoyaltyEventDTO getRepresentation(){
        LoyaltyEventDTO dto = new LoyaltyEventDTO();
        dto.setId(getId());
        dto.setName(getName());
        dto.setOrganizationId(getOrganization().getId());
        dto.setIsActive(getIsActive());
        dto.setStartDate(getStartDate());
        dto.setEndDate(getEndDate());
        return  dto;
    }
}
