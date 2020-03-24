package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ShopThreeSixtyRequestDTO {

    @JsonProperty("view360_id")
    private Long view360Id;

    @JsonProperty("floors")
    private ShopFloorsRequestDTO shopFloorsRequestDTO;
}
