package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class ShopThreeSixtyDTO extends BaseRepresentationObject{

    private Long id;
    @JsonProperty("scene_name")
    private String name;

    @JsonProperty("shop_id")
    private Long shopId;
}
