package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import lombok.Data;

import javax.persistence.*;


@Table(name = "organization_tags")
@Entity
@Data
public class OrganizationTagsEntity implements BaseEntity{

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
        // TODO Auto-generated method stub
        return null;
    }

}
