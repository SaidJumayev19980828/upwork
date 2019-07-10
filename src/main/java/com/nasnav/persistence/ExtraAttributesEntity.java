package com.nasnav.persistence;


import javax.persistence.*;

import com.nasnav.dto.ExtraAttributesRepresentationObject;
import org.springframework.data.jpa.domain.AbstractPersistable;
import com.nasnav.dto.BaseRepresentationObject;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Entity
@Table(name = "extra_attributes")
@Data
@EqualsAndHashCode(callSuper = false)

public class ExtraAttributesEntity extends AbstractPersistable<Long> implements BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "key_name")
    private String name;

    @Column(name = "attribute_type")
    private String type;

    @Column(name = "organization_id")
    private Long organizationId;

   /* @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    private OrganizationEntity organizationEntity;*/

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
