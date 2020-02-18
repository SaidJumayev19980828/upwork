package com.nasnav.integration.sallab.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RecentItem {

    @JsonProperty("AttributesObject")
    private Attributes attributesObject;

    @JsonProperty("Id")
    private String id;

    @JsonProperty("Name")
    private String name;

}
