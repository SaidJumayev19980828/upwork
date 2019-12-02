package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OrganizationTagsDTO {

    private Long id;
    @JsonProperty(value = "tag_id")
    private Long tagId;
    private String alias;
    private String metadata;
    @JsonProperty(required = true)
    private String operation;

}
