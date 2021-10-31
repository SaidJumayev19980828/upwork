package com.nasnav.persistence;

import com.nasnav.dto.request.CharityDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "charity")
@Data
public class CharityEntity {

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

    public CharityDTO getRepresentation() {
        CharityDTO dto = new CharityDTO();
        BeanUtils.copyProperties(this, dto);
        dto.setOrgId(organization.getId());
        return dto;
    }

}
