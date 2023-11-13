package com.nasnav.persistence;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "organization_services")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationServicesEntity extends DefaultBusinessEntity<Long>{

    @Column(name = "org_id")
    private Long orgId;
    @Column(name = "service_id")
    private Long serviceId;
    private Boolean enabled;

}
