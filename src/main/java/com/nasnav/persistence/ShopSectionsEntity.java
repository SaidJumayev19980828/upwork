package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;
import java.util.stream.Collectors;
import static java.util.Comparator.comparing;

@Table(name = "shop_sections")
@Entity
@Data
@EqualsAndHashCode(callSuper=false)
public class ShopSectionsEntity implements BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "shop_floor_id", nullable = false)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ShopFloorsEntity shopFloorsEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private OrganizationEntity organizationEntity;

    @OneToMany(mappedBy = "shopSectionsEntity", cascade = CascadeType.REMOVE)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ShopScenesEntity> shopScenes;

    @Column(name = "name")
    private String name;

    @Column(name = "image")
    private String image;



    @Override
    public BaseRepresentationObject getRepresentation() {
        ShopSectionsDTO section = new ShopSectionsDTO();

        section.setId(getId());
        section.setName(getName());
        section.setShopScenes(getShopScenes().stream()
                                             .map(scene -> (ShopScenesDTO) scene.getRepresentation())
                                             .sorted(comparing(ShopScenesDTO::getId))
                                             .collect(Collectors.toList()));

        if (getImage() != null)
            section.setImage(createImage(getImage()));

        return section;
    }

    private Image createImage(String imageFile) {
        Image image = new Image(imageFile);/*,
                                new ImageUrl(pathUri+imageFile),
                         null,
                                new ImageUrl(pathUri+imageFile));*/
        return image;
    }
}
