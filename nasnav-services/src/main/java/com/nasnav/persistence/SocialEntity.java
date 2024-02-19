package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.SocialRepresentationObject;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;

@Data
@Table
@Entity(name = "social_links")
public class SocialEntity implements BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String facebook;
    private String twitter;
    private String instagram;
    private String youtube;
    private String linkedin;
    private String pinterest;
    private String tiktok;
    private String whatsapp;
    


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    @JsonIgnore
    private OrganizationEntity organizationEntity;

    @Override
    public BaseRepresentationObject getRepresentation() {
        SocialRepresentationObject socialRepresentationObject = new SocialRepresentationObject();
        BeanUtils.copyProperties(this, socialRepresentationObject);
        return socialRepresentationObject;
    }
}
