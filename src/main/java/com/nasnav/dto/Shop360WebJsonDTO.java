package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class Shop360WebJsonDTO {

    @JsonProperty("default")
    private DefaultDTO defaultDTO;

    private Map<String, SceneDTO> scenes;
}
