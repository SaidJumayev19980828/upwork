package com.nasnav.persistence;


import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.ExtraAttributesRepresentationObject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "extra_attributes")
@Data
@EqualsAndHashCode(callSuper = false)

public class ExtraAttributesEntity implements BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "key_name")
    private String name;

    @Column(name = "attribute_type")
    private String type;

    @Column(name = "organization_id")
    private Long organizationId;


    @Column(name = "icon")
    private String iconUrl;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    @Override
    public BaseRepresentationObject getRepresentation() {
        ExtraAttributesRepresentationObject extraAttributesRepresentationObject = new ExtraAttributesRepresentationObject();
        extraAttributesRepresentationObject.setId(getId());
        extraAttributesRepresentationObject.setName(getName());
        extraAttributesRepresentationObject.setType(getType());
        extraAttributesRepresentationObject.setIconUrl(getIconUrl());
        return extraAttributesRepresentationObject;
    }

}
