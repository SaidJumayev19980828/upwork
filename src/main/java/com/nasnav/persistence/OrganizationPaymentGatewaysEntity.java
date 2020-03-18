package com.nasnav.persistence;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name="organization_payments")
@Data
@EqualsAndHashCode(callSuper=false)
public class OrganizationPaymentGatewaysEntity implements BaseEntity{

    @Id
    @GeneratedValue(strategy= IDENTITY)
    private Integer id;

    @Column(name="organization_id")
    private Long organizationId;

    @Column(name="gateway")
    private String gateway;

    @Column(name="account")
    private String account;

    @Override
    public BaseRepresentationObject getRepresentation() {
        return null;
    }
}
