package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TagsTreeNodeCreationDTO {

    @JsonProperty("tag_id")
    private Long tagId;
        
    @JsonProperty(value = "children")
    private List<TagsTreeNodeCreationDTO> children;
}
