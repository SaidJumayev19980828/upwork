package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@JsonPropertyOrder ({"id", "shop_section_id", "name", "image"})

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = false)
public class ShopScenesDTO extends BaseRepresentationObject{
    private Long id;

    @JsonProperty("shop_section_id")
    private Long shopSectionId;

    private String name;

    private Image image;

    private String resized;

    private String thumbnail;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
