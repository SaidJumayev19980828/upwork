package com.nasnav.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class TagsRepresentationObject extends BaseRepresentationObject {

    private Long id;
    private String name;
    private String alias;
    private String metadata;
    @JsonProperty("p_name")
    private String pname;
    @JsonProperty(value = "category_id")
    private Long categoryId;
    @JsonProperty(value = "graph_id")
    private Integer graphId;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<TagsRepresentationObject> children;

    public TagsRepresentationObject() {
        children = new ArrayList<>();
    }

}
