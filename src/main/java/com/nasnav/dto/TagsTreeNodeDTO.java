package com.nasnav.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class TagsTreeNodeDTO {
	private Long id;
	@JsonProperty("tag_id")
	private Long tagId;
	@JsonProperty("node_id")
	private Long nodeId;
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
    public List<TagsTreeNodeDTO> children;

    public TagsTreeNodeDTO() {
        children = new ArrayList<>();
    }
}
