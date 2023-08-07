package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.OrganizationThemesRepresentationObject;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "organization_themes")
@Data
public class OrganizationThemeEntity implements BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "first_color")
    private String firstColor;
    
    @Column(name = "second_color")
    private String secondColor;
    
    @Column(name = "first_section")
    private Boolean firstSection;
    
    @Column(name = "first_section_product")
    private Integer firstSectionProduct;
    
    @Column(name = "first_section_image")
    private String firstSectionImage;
    
    private String logo;

    private String cover;
    
    @Column(name = "second_section")
    private Boolean secondSection;
    
    @Column(name = "second_section_product")
    private Integer secondSectionProduct;
    
    @Column(name = "second_section_image")
    private String secondSectionImage;
    
    @Column(name = "slider_body")
    private Boolean sliderBody;
    
    @Column(name = "slider_header")
    private String sliderHeader;
    
    @Column(name = "slider_images")
    @Type(type = "com.nasnav.persistence.GenericArrayType")
    private String[] sliderImages;
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;


    
    @ManyToOne
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    @JsonIgnore
    private OrganizationEntity organizationEntity;

    @Override
    public BaseRepresentationObject getRepresentation() {
        OrganizationThemesRepresentationObject organizationThemesRepresentationObject = new OrganizationThemesRepresentationObject();
        organizationThemesRepresentationObject.setFirstColor(getFirstColor());
        organizationThemesRepresentationObject.setFirstSection(getFirstSection());
        organizationThemesRepresentationObject.setFirstSectionImageUrl(getFirstSectionImage());
        organizationThemesRepresentationObject.setLogoUrl(getLogo());
        organizationThemesRepresentationObject.setCoverUrl(getCover());
        organizationThemesRepresentationObject.setFirstSectionProduct(getFirstSectionProduct());
        organizationThemesRepresentationObject.setSecondColor(getSecondColor());
        organizationThemesRepresentationObject.setSecondSection(getSecondSection());
        organizationThemesRepresentationObject.setSecondSectionImageUrl(getSecondSectionImage());
        organizationThemesRepresentationObject.setSecondSectionProduct(getSecondSectionProduct());
        organizationThemesRepresentationObject.setSliderBody(getSliderBody());
        organizationThemesRepresentationObject.setSliderHeader(getSliderHeader());
        organizationThemesRepresentationObject.setSliderImagesUrls(getSliderImages());
        return organizationThemesRepresentationObject;
    }
}
