package com.nasnav.persistence;

import com.nasnav.dto.request.CoinsDropDTO;
import com.nasnav.dto.request.CoinsDropLogsDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="coins_drop_logs")
@Data
public class CoinsDropLogsEntity {

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
    private CoinsDropEntity coinsDrop;

    @ManyToOne
    @JoinColumn(name = "created_by")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity user;

    @Column(name="is_active")
    private Boolean isActive;

    public CoinsDropLogsDTO getRepresentation() {
        CoinsDropLogsDTO dto = new CoinsDropLogsDTO();
        BeanUtils.copyProperties(this, dto);
        dto.setOrgId(organization.getId());
        dto.setUserId(user.getId());
        return dto;
    }
}
