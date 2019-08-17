package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "name",
        "p_name",
        "logo_url",
        "parent_id"
})

@Data
@EqualsAndHashCode(callSuper=true)
public class CategoryRepresentationObject extends BaseRepresentationObject{

    @JsonProperty("id")
    private Long id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("p_name")
    private String pname;
    @JsonProperty("logo_url")
    private String logoUrl;
    @JsonProperty("parent_id")
    private Integer parentId;
    @JsonProperty("operation")
    private String operation;
}
