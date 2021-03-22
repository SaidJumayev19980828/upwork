package com.nasnav.persistence;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.*;

@Entity
@Table(name="organization_domains")
@Data
@EqualsAndHashCode(callSuper=false)
public class OrganizationDomainsEntity implements BaseEntity{

    @Id
    @GeneratedValue(strategy= IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    @JsonIgnore
    private OrganizationEntity organizationEntity;

    @Column(name="domain")
    private String domain;

    @Column(name="subdir")
    private String subdir;

    @Column(name="canonical")
    private Integer priority;

    @Override
    public BaseRepresentationObject getRepresentation() {
        return null;
    }
}
