package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TagsTreeNodeDTO {

    @JsonProperty("tag_id")
    private Long tagId;
        
    @JsonProperty(value = "children_nodes")
    private List<TagsTreeNodeDTO> children;
}
