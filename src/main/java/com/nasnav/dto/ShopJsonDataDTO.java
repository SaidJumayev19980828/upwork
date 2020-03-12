package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ShopJsonDataDTO {

    @JsonProperty("view360_id")
    private Long view360Id;

    private String type;

    private String json;

}
