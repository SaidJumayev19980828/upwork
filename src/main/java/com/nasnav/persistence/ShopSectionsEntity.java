package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.Image;
import com.nasnav.dto.ShopScenesDTO;
import com.nasnav.dto.ShopSectionsDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Table(name = "shop_sections")
@Entity
@Data
@EqualsAndHashCode(callSuper=false)
public class ShopSectionsEntity implements BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
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

    @OneToMany(mappedBy = "shopSectionsEntity")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<ShopScenesEntity> shopScenes;

    @Column(name = "name")
    private String name;

    @Column(name = "web_json_data")
    private String webJsonData;

    @Column(name = "mobile_json_data")
    private String mobileJsonData;

    @Column(name = "image")
    private String image;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @CreationTimestamp
    private LocalDateTime updatedAt;

    @Override
    public BaseRepresentationObject getRepresentation() {
        ShopSectionsDTO section = new ShopSectionsDTO();

        ObjectMapper mapper = new ObjectMapper();
        try {
            section.setImage(mapper.readValue(getImage(), Image.class));
        } catch (IOException e) {

        }

        section.setId(getId());
        section.setName(getName());
        section.setShopScenes(getShopScenes().stream().map(scene -> (ShopScenesDTO) scene.getRepresentation()).collect(Collectors.toSet()));

        return section;
    }
}
