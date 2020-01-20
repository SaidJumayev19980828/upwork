package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TagsLinkDTO extends BaseJsonDTO{

    @JsonProperty("parent_id")
    private Long parentId;
    @JsonProperty("children_ids")
    private List<Long> childrenIds;

    public void setParentId(Long parentId) {
        setPropertyAsUpdated("parentId");
        this.parentId = parentId;
    }

    public void setChildrenIds(List<Long> childrenIds) {
        setPropertyAsUpdated("childrenIds");
        this.childrenIds = childrenIds;
    }

    @Override
    protected void initRequiredProperties() {

    }
}
