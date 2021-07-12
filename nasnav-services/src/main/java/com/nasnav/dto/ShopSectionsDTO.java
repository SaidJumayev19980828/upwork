package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = false)
public class ShopSectionsDTO extends BaseRepresentationObject{

    private Long id;

    private String name;

    @JsonProperty( "web_json_data")
    private String webJsonData;

    @JsonProperty("mobile_json_data")
    private String mobileJsonData;

    private Image image;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    private Integer priority;

    @JsonProperty("scenes")
    private List<ShopScenesDTO> shopScenes;
}
