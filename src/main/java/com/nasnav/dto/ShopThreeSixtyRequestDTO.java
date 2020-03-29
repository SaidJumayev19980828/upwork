package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ShopThreeSixtyRequestDTO {

    @JsonProperty("view360_id")
    private Long view360Id;

    @JsonProperty("floors")
    private List<ShopFloorsRequestDTO> shopFloorsRequestDTO;
}
