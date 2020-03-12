package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TagsTreeCreationDTO {

    @JsonProperty(value = "nodes")
    private List<TagsTreeNodeDTO> treeNodes;
}
