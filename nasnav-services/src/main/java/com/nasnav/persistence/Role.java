package com.nasnav.persistence;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;


@Data
@Entity
@Table(name = "roles")
@EqualsAndHashCode(callSuper=false)
public class Role extends DefaultBusinessEntity<Integer>{

    @Column(name = "name")
    private String name;

    @Column(name = "organization_id")
    private Long organizationId;
}
