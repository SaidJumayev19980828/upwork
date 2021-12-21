package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@JsonInclude(JsonInclude.Include.NON_NULL)

@Data
@EqualsAndHashCode(callSuper=true)
public class CategoryRepresentationObject extends BaseRepresentationObject{
    private Long id;
    private String name;
    @JsonProperty("p_name")
    private String pname;
    @JsonProperty("logo_url")
    private String logo;
    private String cover;
    @JsonProperty("cover_small")
    private String coverSmall;
    @JsonProperty("parent_id")
    private Long parentId;
    private String operation;
}
