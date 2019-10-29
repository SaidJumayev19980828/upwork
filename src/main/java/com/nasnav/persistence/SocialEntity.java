package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.SocialRepresentationObject;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

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
    
    @Column(name = "created_at")
    private Date createdAt;
    
    @Column(name = "updated_at")
    private Date updatedAt;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    @JsonIgnore
    private OrganizationEntity organizationEntity;

    @Override
    public BaseRepresentationObject getRepresentation() {

        SocialRepresentationObject socialRepresentationObject = new SocialRepresentationObject();
        socialRepresentationObject.setFacebook(getFacebook());
        socialRepresentationObject.setTwitter(getTwitter());
        socialRepresentationObject.setInstagram(getInstagram());
        return socialRepresentationObject;
    }
}
