package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TagsDTO extends BaseJsonDTO{

    private Long id;
    @JsonProperty(value = "category_id")
    private Long categoryId;
    private String name;
    private String alias;
    private String metadata;
    @JsonProperty(required = true)
    private String operation;
    @JsonProperty(value = "graph_id")
    private Integer graphId;
    @JsonProperty(value = "has_category")
    private boolean hasCategory;
    
    
    public TagsDTO() {
    	hasCategory = true;
    }
    
    
    @Override
    protected void initRequiredProperties() { }

    public void setGraphId(Integer graphId) {
        setPropertyAsUpdated("graphId");
        this.graphId = graphId;
    }
}
