package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.OrganizationImagesRepresentationObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;

@Entity
@Table(name="organization_images")
@Data
@EqualsAndHashCode(callSuper=false)
public class OrganizationImagesEntity extends AbstractPersistable<Long> implements BaseEntity{

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    @JsonIgnore
    private OrganizationEntity organizationEntity;

    @OneToOne
    @JoinColumn(name = "shop_id", referencedColumnName = "id")
    @JsonIgnore
    private ShopsEntity shopsEntity;

    @Column(name="type")
    private Integer type;

    @Column(name="uri")
    private String uri;

    @Override
    public BaseRepresentationObject getRepresentation() {
        OrganizationImagesRepresentationObject orgImgRepObj = new OrganizationImagesRepresentationObject();
        orgImgRepObj.setId(getId());
        //orgImgRepObj.setOrganizationId(getOrganizationEntity().getId());
        if (getShopsEntity() != null)
            orgImgRepObj.setShopId(getShopsEntity().getId());
        orgImgRepObj.setType(getType());
        orgImgRepObj.setUri(getUri());
        return orgImgRepObj;
    }
}

