package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.OrganizationTagsRepresentationObject;
import lombok.Data;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;


@Table(name = "organization_tags")
@Entity
@Data
public class OrganizationTagsEntity extends AbstractPersistable<Long> implements BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alias")
    private String alias;

    @Column(name = "p_name")
    private String pname;

    @Column(name = "logo")
    private String logo;

    @Column(name = "banner")
    private String banner;

    @OneToOne
    @JoinColumn(name = "tag_id", referencedColumnName = "id")
    @JsonIgnore
    private TagsEntity tagsEntity;

    @OneToOne
    @JoinColumn(name = "org_id", referencedColumnName = "id")
    @JsonIgnore
    private OrganizationEntity organizationEntity;

    @Override
    public BaseRepresentationObject getRepresentation() {
        OrganizationTagsRepresentationObject obj = new OrganizationTagsRepresentationObject();
        obj.setId(getId());
        obj.setAlias(getAlias());
        obj.setPname(getPname());
        obj.setLogo(getLogo());
        obj.setBanner(getBanner());

        return obj;
    }

}
