package com.nasnav.dto;

import java.util.ArrayList;
import java.util.List;

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
    private Long categoryId;
    public List<TagsRepresentationObject> children;

    public TagsRepresentationObject() {
        children = new ArrayList<>();
    }

}
