package com.nasnav.persistence;


import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.ExtraAttributesRepresentationObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Entity
@Table(name = "extra_attributes")
@Data
@EqualsAndHashCode(callSuper = false)
public class ExtraAttributesEntity implements BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "key_name")
    private String name;

    @Column(name = "attribute_type")
    private String type;

    @Column(name = "organization_id")
    private Long organizationId;


    @Column(name = "icon")
    private String iconUrl;


    
    

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
