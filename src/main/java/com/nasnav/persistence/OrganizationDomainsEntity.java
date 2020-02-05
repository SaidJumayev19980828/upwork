package com.nasnav.persistence;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Entity
@Table(name="organization_domains")
@Data
@EqualsAndHashCode(callSuper=false)
public class OrganizationDomainsEntity implements BaseEntity{

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    @JsonIgnore
    private OrganizationEntity organizationEntity;

    @Column(name="domain")
    private String domain;

    @Override
    public BaseRepresentationObject getRepresentation() {
        return null;
    }
}
