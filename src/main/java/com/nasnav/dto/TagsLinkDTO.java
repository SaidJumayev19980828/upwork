package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TagsLinkDTO {

    @JsonProperty("parent_id")
    private Long parentId;
    @JsonProperty("children_ids")
    private List<Long> childrenIds;
}
