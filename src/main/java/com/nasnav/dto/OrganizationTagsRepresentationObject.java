package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OrganizationTagsRepresentationObject {

    private Long id;
    private String alias;
    private String logo;
    private String banner;
    @JsonProperty("p_name")
    private String pname;
}
