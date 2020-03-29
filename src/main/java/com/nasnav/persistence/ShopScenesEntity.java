package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.Image;
import com.nasnav.dto.ImageUrl;
import com.nasnav.dto.ShopScenesDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.IOException;
import java.time.LocalDateTime;

@Table(name = "scenes")
@Entity
@Data
@EqualsAndHashCode(callSuper=false)
public class ShopScenesEntity implements BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
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

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @CreationTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "image")
    private String image;

    @Column(name = "resized")
    private String resized;

    @Column(name = "thumbnail")
    private String thumbnail;

    @Override
    public BaseRepresentationObject getRepresentation() {
        ShopScenesDTO scene = new ShopScenesDTO();

        scene.setId(getId());
        scene.setName(getName());
        scene.setShopSectionId(getShopSectionsEntity().getId());
        scene.setThumbnail(getThumbnail());
        scene.setResized(getResized());

        if (getImage() != null)
            scene.setImage(createImage(getImage()));

        return scene;
    }

    private Image createImage(String imageFile) {
        String pathUri = "/uploads/scene/image/" + getId() + "/";
        Image image = new Image(pathUri+imageFile,
                new ImageUrl(pathUri+"thumb_"+imageFile),
                new ImageUrl(pathUri+"resized_"+imageFile),
                null);
        return image;
    }
}
