package com.nasnav.persistence;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@Table(name = "package_registered")
@Entity
@EntityListeners({AuditingEntityListener.class})
public class PackageRegisteredEntity extends DefaultBusinessEntity<Long> {
    @OneToOne(optional = false)
    @JoinColumn(name = "org_id", referencedColumnName = "id")
    private OrganizationEntity organization;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "package_id", referencedColumnName = "id")
    private PackageEntity packageEntity;

    @Column(name = "registered_date")
    @LastModifiedDate
    private Date registeredDate;

    @ManyToOne(cascade = CascadeType.MERGE, optional = false)
    @JoinColumn(name = "creator_employee_id", referencedColumnName = "id")
    // @CreatedBy // spring doesn't support muliple AuditorAware (yet?)
    private EmployeeUserEntity creatorEmployee;

    public PackageRegisteredEntity(OrganizationEntity organization) {
        this.organization = organization;
    }
}
