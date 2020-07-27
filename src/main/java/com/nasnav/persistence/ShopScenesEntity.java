package com.nasnav.persistence;

import java.time.LocalDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.Image;
import com.nasnav.dto.ImageUrl;
import com.nasnav.dto.ShopScenesDTO;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Table(name = "scenes")
@Entity
@Data
@EqualsAndHashCode(callSuper=false)
public class ShopScenesEntity implements BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "shop_section_id", nullable = false)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ShopSectionsEntity shopSectionsEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private OrganizationEntity organizationEntity;

    @Column(name = "name")
    private String name;



    @Column(name = "image")
    private String image;

    @Column(name = "resized")
    private String resized;

    @Column(name = "thumbnail")
    private String thumbnail;

    private Integer priority;

    @Override
    public BaseRepresentationObject getRepresentation() {
        ShopScenesDTO scene = new ShopScenesDTO();

        scene.setId(getId());
        scene.setName(getName());
        scene.setShopSectionId(getShopSectionsEntity().getId());
        scene.setImage(createImage(getImage(), getResized(), getThumbnail()));
        scene.setPriority(getPriority());

        return scene;
    }

    private Image createImage(String imageFile, String resized, String thumbnail) {
        Image image = new Image(imageFile,
                new ImageUrl(thumbnail),
                new ImageUrl(resized),
                null);
        return image;
    }
}
