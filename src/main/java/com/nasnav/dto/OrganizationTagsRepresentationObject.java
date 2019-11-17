package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class OrganizationTagsRepresentationObject extends BaseRepresentationObject {

    private Long id;
    private String alias;
    private String logo;
    private String banner;
    @JsonProperty("p_name")
    private String pname;
    private List<OrganizationTagsRepresentationObject> children;
}
