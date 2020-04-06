package com.nasnav.persistence;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.ShopThreeSixtyDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Table(name = "shop360s")
@Entity
@Data
@EqualsAndHashCode(callSuper=false)
public class ShopThreeSixtyEntity implements BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ShopsEntity shopsEntity;

    @Column(name = "web_json_data")
    private String webJsonData;

    @Column(name = "url")
    private String url;

    @Column(name = "scene_asset_bundle")
    private String sceneAssetBundle;

    @Column(name = "scene_name")
    private String sceneName;

    @Column(name = "mobile_json_data")
    private String mobileJsonData;

    @Column(name = "published")
    private boolean published;

    @Column(name = "preview_json_data")
    private String previewJsonSata;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @CreationTimestamp
    private LocalDateTime updatedAt;

    @Override
    public BaseRepresentationObject getRepresentation() {
        ShopThreeSixtyDTO shopDTO = new ShopThreeSixtyDTO();
        shopDTO.setId(getId());
        shopDTO.setName(getSceneName());
        return shopDTO;
    }
}
